package osh.old.busdriver.wago;

import osh.core.logging.IGlobalLogger;
import osh.old.busdriver.wago.data.WagoDeviceList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;


/**
 * This class handles a connection to the wago controller. It starts its own
 * thread in the contstructor named "TCPUDPConnectionHandler for xxxx". It opens
 * a pair of connections, one tcp connection to receive the current xml documents
 * from the controller, and one udp "connection" to send commands.
 * The xml documents are parsed by a separate parser, which must register itself
 * via <code>registerParser</code>. Commands are sent by instances of subclasses
 * of <code>CommandGenerator</code>.
 *
 * @author Kaibin Bao, Till Schuberth
 */
public class TCPUDPConnectionHandler implements Runnable {
    // Port of Wago 750-860 Protocol is 9155
    private final int controllerPort = 9155;
    private final Object commandCounterLock = new Object();
    private final IGlobalLogger logger;
    private final Charset ascii = StandardCharsets.US_ASCII;
    private InetAddress controllerAddr;
    private Socket dataSocket;
    private DatagramSocket cmdSocket;
    private byte commandCounter = 1;
    private boolean shutdown;
    private DocumentParsedListener listener;
    private ConnectionStatusListener connectionListener;
    private int parseErrorCounter;
    private LocalDateTime lastParseErrorCounterReset = LocalDateTime.now();
    private int reconnectWait;
    private LocalDateTime lastIOException = LocalDateTime.now();

    /* *****************
     * inner classes
     */

    /**
     * CONSTRUCTOR
     *
     * @param logger
     * @param address ip address of the wago controller
     * @throws SmartPlugException
     */
    public TCPUDPConnectionHandler(IGlobalLogger logger, InetAddress address) throws SmartPlugException {
        this.logger = logger;

        try {
            this.controllerAddr = address;
            this.cmdSocket = new DatagramSocket();
            this.cmdSocket.setSoTimeout(3000);
        } catch (SocketException e) {
            throw new SmartPlugException("could not initialize datagram socket", e);
        }

        // let the thread initialize the data socket
        /*
        try {
            initDataSocket();
        } catch (IOException e1) {
            logger.logError("IOException in wago connection handler; trying to continue...", e1);
        }
        */

        try {
            this.sendUdpPacket("RE", (byte) 0);
        } catch (IOException e) {
            logger.logWarning("wago controller reset failed (host: " + this.controllerAddr.toString() + ")", e);
        }

        new Thread(this, "TCPUDPConnectionHandler for " + this.controllerAddr.toString()).start();
    }

    private void sendUdpPacket(String data, byte session) throws IOException {
        this.sendUdpPacket(data.getBytes(this.ascii), session);
    }

    /**
     * send out udp command packet
     *
     * @param data
     * @param session session byte to be used for this command (1 command = 1 session)
     * @throws IOException
     */
    private synchronized void sendUdpPacket(byte[] data, byte session) throws IOException {
        if (this.cmdSocket == null) throw new IOException("socket still uninitialized");

        DatagramPacket _p = new DatagramPacket(data, data.length);
        _p.setAddress(this.controllerAddr);
        _p.setPort(this.controllerPort);
        DatagramPacket _r = new DatagramPacket(new byte[100], 100);
        _r.setAddress(this.controllerAddr);
        _r.setPort(this.controllerPort);

        for (int i = 0; i < 3; i++) {
            this.cmdSocket.send(_p);
            try {
                this.cmdSocket.receive(_r);
            } catch (SocketTimeoutException e) {
                continue;
            }

            byte[] ack = Arrays.copyOf("ACK".getBytes(this.ascii), 4);
            ack[3] = session;

            if (!Arrays.equals(Arrays.copyOf(_r.getData(), _r.getLength()), ack))
                break; //pretty inefficient, but what the hell...

            return;
        }

        String resp = new String(_r.getData(), this.ascii);
        throw new IOException("No answer from controller. Answer: \"" + resp + "\"");
    }

    private byte getSessionId() {
        byte retVal;

        synchronized (this.commandCounterLock) {
            retVal = this.commandCounter;

            this.commandCounter++;
            if (this.commandCounter == 0)
                this.commandCounter = 1;

        }

        return retVal;
    }

    /**
     * send command to wago controller ("COxblahblah")
     */
    private void sendCommand(String cmd) throws SmartPlugException {
        byte[] command;

        //generate session byte for this command
        byte session = this.getSessionId();

        byte[] baCmd = cmd.getBytes(this.ascii);
        command = new byte[baCmd.length + 3];
        command[0] = 'C';
        command[1] = 'O';
        command[2] = session;
        System.arraycopy(baCmd, 0, command, 3, baCmd.length);


        try {
            this.sendUdpPacket(command, session);
        } catch (IOException e) {
            throw new SmartPlugException("could not send smart plug command", e);
        }
    }


    /* ###########
     * # methods #
     * ########### */

    private void initDataSocket() throws IOException {
        this.dataSocket = new Socket(this.controllerAddr, this.controllerPort);
        this.dataSocket.setSoTimeout(5000);
    }

    @Override
    public void run() {
        JAXBContext context;

        try {
            context = JAXBContext.newInstance(WagoDeviceList.class);
        } catch (JAXBException e1) {
            this.logger.logError("unable to initialize XML marshaller", e1);
            return;
        }

        TokenizingInputStream is = null;
        OutputStream os = null;

        while (!this.shutdown) {
            try {
                //check connection
                if (this.dataSocket == null) {
                    this.initDataSocket();
                }
                if (!this.dataSocket.isConnected() || this.dataSocket.isClosed()) {
                    try {
                        // closes also the input-/ outputstream
                        this.dataSocket.close();
                    } catch (IOException ignored) {
                    }

                    this.logger.logWarning("controller " + this.controllerAddr.toString() + " is not connected");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    this.initDataSocket();
                }

                //is is a single xml document
                is = new TokenizingInputStream(this.dataSocket.getInputStream());
                os = this.dataSocket.getOutputStream();

                if (this.connectionListener != null) this.connectionListener.connectionEvent(true);

                //parse document with all registered parsers
                while (is.hasNext() && !this.shutdown) {
                    while (is.available() == 0) {
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException ignored) {
                        } // ignore
                        // send data request if enough time has passed
                        os.write(42);
                        os.flush();
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ignored) {
                        } // ignore
                    }

                    // Process the XML
                    try {
                        Unmarshaller unmarshaller = context.createUnmarshaller();
                        WagoDeviceList deviceList = (WagoDeviceList) unmarshaller.unmarshal(is);
                        this.listener.documentParsedEvent(deviceList);

                        if (this.connectionListener != null) this.connectionListener.connectionEvent(true);
                    } catch (JAXBException e) {
                        this.handleParseException(e);
                        this.logger.logError("failed to unmarshal wago homebus xml", e);
                        //deviceList.setDevices(Collections.<MieleDeviceHomeBusData>emptyList()); // set empty list

                        if (this.connectionListener != null) this.connectionListener.connectionEvent(false);
                    } finally {
                        is.skip();
                        is.next();
                    }
                }
            } catch (IOException e1) {
                if (e1 instanceof NoRouteToHostException) {
                    this.logger.logWarning("It's dead, Jim: No route to " + this.controllerAddr);
                } else {
                    this.logger.logError("IOException in wago connection handler; trying to continue...", e1);
                }
                long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                        - this.lastIOException.toEpochSecond(ZoneOffset.UTC);
                if (diff < 0 || diff > 300000) {
                    this.reconnectWait = 0;
                } else {
                    if (this.reconnectWait <= 0) this.reconnectWait = 1;
                    this.reconnectWait *= 2;
                    if (this.reconnectWait > 180) this.reconnectWait = 180;
                }
                this.lastIOException = LocalDateTime.now();

                try { // just for safety
                    if (is != null)
                        is.reallyClose();
                    if (os != null)
                        os.close();
                } catch (IOException ignored) {
                }
            } catch (Exception e) {
                //prevent thread from dying
                this.logger.logError("fatal error, FIXME:", e);
            }

            if (this.connectionListener != null) this.connectionListener.connectionEvent(false);

            try {
                Thread.sleep(this.reconnectWait * 1000);
            } catch (InterruptedException ignored) {
            }
        }

        try {
            this.dataSocket.close();
        } catch (IOException ignored) {
        }
    }

    private void handleParseException(Exception e) {
        long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                - this.lastParseErrorCounterReset.toEpochSecond(ZoneOffset.UTC);
        if (diff < 0 || diff > 30000L) {
            this.parseErrorCounter = 0;
            this.lastParseErrorCounterReset = LocalDateTime.now();
        }
        this.parseErrorCounter++;

        //if there are more than three parse exceptions in 30 seconds, log them
        if (this.parseErrorCounter >= 3) {
            this.logger.logError("parse error; trying to continue...", e);
        }
    }

    public DocumentParsedListener getListener() {
        return this.listener;
    }

    public void setListener(DocumentParsedListener listener) {
        this.listener = listener;
    }

    public ConnectionStatusListener getConnectionListener() {
        return this.connectionListener;
    }

    public void setConnectionListener(
            ConnectionStatusListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    /* ***************************
     * simple methods
     */

    public void shutdown() {
        this.shutdown = true;
    }

    public interface DocumentParsedListener {
        void documentParsedEvent(WagoDeviceList deviceList);
    }

    public interface ConnectionStatusListener {
        void connectionEvent(boolean isConnected);
    }

    /**
     * split tcp stream into xml documents (separated by a NUL-character)
     * This input stream reads from the source until a NUL-character appears.
     * The NUL-character will not be copied to the outgoing stream.
     *
     * @author userx
     */
    private static class TokenizingInputStream extends InputStream {

        private final InputStream stream;
        private boolean hasMore = true, endOfToken;

        public TokenizingInputStream(InputStream stream) {
            this.stream = new BufferedInputStream(stream);
        }

        public boolean hasNext() {
            return this.hasMore;
        }

        public void next() {
            this.endOfToken = false;
        }

        @Override
        public int available() throws IOException {
            return this.stream.available();
        }

        @Override
        public void close() throws IOException {
            this.skip();

            if (!this.hasMore) {
                this.stream.close();
            }
        }

        public void reallyClose() throws IOException {
            this.stream.close();
        }

        /**
         * Skip one message
         *
         * @throws IOException
         */
        public void skip() throws IOException {
            while (this.read() >= 0) ;
        }

        @Override
        public int read() throws IOException {
            if (this.endOfToken) return -1;

            int ret = this.stream.read();
            if (ret == 0) {
                this.endOfToken = true;
                return -1;
            } else if (ret == -1) {
                this.hasMore = false;
                return -1;
            } else {
                return ret;
            }
        }
    }

    /**
     * abstract super class for a command sent to the wago controller (e. g. for
     * a digital output module)
     */
    public static abstract class CommandGenerator {
        protected void sendCommand(String device, String moduleId, String functionId, String command, TCPUDPConnectionHandler instance) throws SmartPlugException {
            instance.sendCommand(device + "-" + moduleId + "-" + functionId + "=" + command);
        }

        @Override
        public abstract boolean equals(Object o);

        public abstract void sendCommand() throws SmartPlugException;

        /**
         * @return true if the target is the same as in o, but not necessarily
         * the command
         */
        public abstract boolean equalsTarget(Object o);

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
