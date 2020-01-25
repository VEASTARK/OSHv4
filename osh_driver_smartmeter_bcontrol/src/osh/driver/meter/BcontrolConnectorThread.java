package osh.driver.meter;

import osh.core.logging.IGlobalLogger;
import osh.driver.BcontrolSmartMeterDriver;
import osh.eal.EALTimeDriver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class BcontrolConnectorThread implements Runnable {

    private final String logInPage = "/mum-webservice/0/start.php";
    private final String meteringPage = "/mum-webservice/consumption.php?meter_id=0/";
    private final String heaterPagePart1 = "/unieq/cmsd/sensor/";
    private final String heaterPagePart2 = ".json?idtype=uuid";

    private final IGlobalLogger logger;
    private final EALTimeDriver timer;
    private final BcontrolSmartMeterDriver meterDriver;

    private final String meterURL;
    private final int meterNumber; //  mitte = 14243021

    //	private UUID heaterUUID = UUID.fromString("c1493681-5cfb-45e9-a55c-e65b8aa6ae0d");
    private final UUID heaterUUID;

    private boolean isConnected;
    private boolean isHeaterConnected;
    private boolean isRunning = true;

    private final String password = "pw";

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    /**
     * CONSTRUCTOR
     */
    public BcontrolConnectorThread(
            IGlobalLogger logger,
            EALTimeDriver timer,
            BcontrolSmartMeterDriver meterDriver,
            String meterURL,
            int meterNumber,
            UUID heaterUUID) {

        this.logger = logger;
        this.timer = timer;
        this.meterDriver = meterDriver;
        this.meterURL = meterURL;
        this.meterNumber = meterNumber;
        this.heaterUUID = heaterUUID;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.logger.logDebug(e);
        }

        // set cookie manager
//		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);

        if (this.heaterUUID != null) {
            // works only with one Heater...
            MyAuthenticator myAuth = new MyAuthenticator();
            Authenticator.setDefault(myAuth);
        }

        while (this.isRunning) {

            // check whether meter is connected
            if (!this.isConnected) {
                // connect to meter
                String meterConnectURL = this.getOpenConnectionURLString();
                String reply = "";
                try {
                    reply = this.openConnectionToMeter(meterConnectURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (reply.contains("{\"authentication\":false}")) {
                    this.isConnected = false;
                    this.isHeaterConnected = false;
                } else {
                    this.isConnected = true;
                }
            }


            if (this.isConnected) {
                // read data
                {
                    // read meter data
                    String meterMeteringURL = this.getReadMeterDataURLString();
                    String meterDataString;
                    try {
                        meterDataString = this.readMeterData(meterMeteringURL);
                        // check whether connection/authentication failed
                        if (meterDataString.contains("{\"authentication\":false}")) {
                            this.isConnected = false;
                            this.isHeaterConnected = false;
                            this.logger.logDebug("FAILED meterDataString=" + meterDataString);
                        } else {
//							logger.logDebug("SUCCESS meterDataString=" + meterDataString);
                            this.meterDriver.receiveMeterMessageFromMeter(meterDataString);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.isConnected = false;
                        this.isHeaterConnected = false;
                    }

                }

                // read heater data
                if (this.heaterUUID != null) {
                    String meterHeaterURL = this.getReadHeaterDataURLString();
                    String heaterDataString;
                    try {
                        if (!this.isHeaterConnected) {
                            this.openConnectionToHeaterReading(meterHeaterURL);
                        }

                        heaterDataString = this.readHeaterData(meterHeaterURL);
                        // check whether connection/authentication failed
                        /*
                         {
                           "status": "failed"
                         }
                        */

                        if (heaterDataString.contains("\"status\": \"failed\"")) {
                            this.isHeaterConnected = false;
                            this.logger.logDebug("FAILED heaterDataString=" + heaterDataString);
                        } else {
                            this.isHeaterConnected = true;
//							logger.logDebug("SUCCESS heaterDataString=" + heaterDataString);
                            try {
                                this.meterDriver.receiveHeaterMessageFromMeter(heaterDataString);
                            } catch (Exception e) {
                                this.logger.logWarning(e.getStackTrace(), e);
                                this.isHeaterConnected = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.isHeaterConnected = false;
                    }

                }


            }

            long lastPull = this.timer.getCurrentEpochSecond();

            try {
                while (lastPull >= this.timer.getCurrentEpochSecond()) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.logger.logDebug(e);
            }
        }

    }


    private String openConnectionToMeter(String meterConnectURL) throws Exception {
        // open up connection to get cookie (PHPSESSID)a
        URLConnection mycon = new URL(meterConnectURL).openConnection();
        mycon.getContent();

        String query = this.getOpenConnectionBody(this.meterNumber);
        mycon = new URL(meterConnectURL).openConnection();
        mycon.setDoOutput(true);
        mycon.setRequestProperty("Accept-Language", "de-DE,de;q=0.8,de-DE;q=0.6,en;q=0.4");
        mycon.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        mycon.setRequestProperty("Accept-Charset", "utf-8");
        mycon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        OutputStream output;
        output = mycon.getOutputStream();
        output.write(query.getBytes(StandardCharsets.UTF_8));
        output.close();
        mycon.getContent();

        // read the output from the server
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                mycon.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        mycon.getInputStream().close();

        return stringBuilder.toString();
    }

    private void openConnectionToHeaterReading(String url) throws Exception {

        /// 1 -> Server redirected too many  times (20)
//		URLConnection connection = new URL(url).openConnection();
//		connection.setRequestProperty("Authorization", "Basic " + (new sun.misc.BASE64Encoder().encode((meterNumber+":"+password).getBytes())));
////		connection.setRequestProperty("Authorization", "Basic " + meterNumber+":"+password);
//		connection.connect();

        /// 2 -> Server redirected too many  times (20)
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        /// 3
//		String encoded = (String) Base64.encode("" + meterNumber + ":" + "pw");
//		connection.setRequestProperty("Authorization", "Basic "+encoded);

        /// 4
//		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//		connection.setRequestMethod("GET");
//		connection.connect();

        /// 5
//		String userPassword = username + ":" + password;
//		String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
//		URLConnection uc = url.openConnection();
//		uc.setRequestProperty("Authorization", "Basic " + encoding);
//		uc.connect();

        try {
            connection.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

//		connection = (HttpURLConnection) new URL(url).openConnection();
//        try {
//			connection.getContent();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println("#########");
    }

    private String readMeterData(String meterMeteringURL) throws Exception {
        URLConnection myConnection = new URL(meterMeteringURL).openConnection();
        myConnection.setDoOutput(true);

//	    String query = getBody(meterNumber);
        myConnection.setRequestProperty("Accept-Language", "de-DE,de;q=0.8,de-DE;q=0.6,en;q=0.4");
        myConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        myConnection.setRequestProperty("Accept-Charset", "utf-8");
        myConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        OutputStream output = myConnection.getOutputStream();
//	    output.write(query.getBytes("utf-8"));
        output.close();
        myConnection.getContent();

        // read the output from the server
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        myConnection.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        myConnection.getInputStream().close();

        return stringBuilder.toString();
    }

    private String readHeaterData(String meterHeaterURL) throws Exception {
        URLConnection myConnection = new URL(meterHeaterURL).openConnection();
        ((HttpURLConnection) myConnection).setRequestMethod("GET");
//	    myConnection.setDoOutput(true);

        myConnection.setRequestProperty("Accept-Language", "de-DE,de,ro;q=0.8,de-DE;q=0.6,en;q=0.4");
        myConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        myConnection.setRequestProperty("Accept-Charset", "utf-8");
        myConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

//	    OutputStream output = myConnection.getOutputStream();
//	    output.close();
        myConnection.getContent();

        // read the output from the server
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        myConnection.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        myConnection.getInputStream().close();

        return stringBuilder.toString();
    }

    private String getOpenConnectionURLString() {
        return this.meterURL + this.logInPage;
    }

    private String getOpenConnectionBody(int meterNumber) {
        return "login=" + meterNumber
                + "&password=" + this.password
                + "&language=de_DE"
                + "&submit=Anmelden"
                + "&datetime=" + this.getCurrentDateAsString();
    }

    private String getReadMeterDataURLString() {
        return this.meterURL + this.meteringPage;
    }

    private String getReadHeaterDataURLString() {
        return this.meterURL + this.heaterPagePart1 + this.heaterUUID + this.heaterPagePart2;
    }

    private String getCurrentDateAsString() {
        return timeFormat.format(ZonedDateTime.now());
    }

    public void shutdown() {
        this.isRunning = false;
    }

    class MyAuthenticator extends Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            System.out.println(">> Authenticator Called for " + this.getRequestingScheme());
            System.out.println(">> with URL " + this.getRequestingURL());
            System.out.println(">> " + BcontrolConnectorThread.this.meterNumber + ":" + BcontrolConnectorThread.this.password);
//        	@SuppressWarnings("restriction")
//			String encPW = new sun.misc.BASE64Encoder().encode((password).getBytes());
            return new PasswordAuthentication("" + BcontrolConnectorThread.this.meterNumber, BcontrolConnectorThread.this.password.toCharArray());
        }
    }


}
