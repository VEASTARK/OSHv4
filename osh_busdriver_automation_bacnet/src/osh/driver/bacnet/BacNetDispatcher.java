/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2009 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 */
package osh.driver.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DefaultDeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetTimeoutException;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.driver.BacNetThermalDriver;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.registry.TimeRegistry;
import osh.registry.interfaces.ITimeRegistryListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

/**
 * BacNet/IP thermal sensors and A/C control
 *
 * @author Kaibin Bao
 */
public class BacNetDispatcher implements ITimeRegistryListener {

    private final IGlobalLogger logger;
    private final TimeRegistry timeRegistry;
    private final Map<BacNetObject, Double> analogInputStates;
    private final Map<BacNetObject, Double> analogValueStates;
    private final Set<String> devices;
    private final int REDISCOVER_INTERVAL = 60;
    private LocalDevice bacNetDevice;
    private int rediscover_countdown = this.REDISCOVER_INTERVAL;
    private boolean STANDALONE;
    public BacNetDispatcher(TimeRegistry timeRegistry, IGlobalLogger logger) {
        this.timeRegistry = timeRegistry;
        this.logger = logger;
        this.analogInputStates = new HashMap<>();
        this.analogValueStates = new HashMap<>();
        this.devices = new HashSet<>();
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {

        BacNetDispatcher drv = new BacNetDispatcher(null, null);
        drv.STANDALONE = true;

        drv.init();

        // Who is
        drv.discover("192.168.1.255", 47808);

        // wie bekomme ich eine Referenz auf den BacNetThermalDriver?
        BacNetThermalDriver driver = new BacNetThermalDriver(null, null, null);

        // Update a few times
        for (int i = 0; i < 2; i++) {
            drv.update();
            System.out.println(drv.toString());
            Thread.sleep(1000);
        }

        // Hannah
        List<BacNetDispatcher.BacNetObject> actuatorObjects = driver.getActuatorObjects();
        for (BacNetObject obj : actuatorObjects) {
            drv.setAnalogValueState(obj, 21.0f);
        }

        // drv.setAnalogValueState(new BacNetObject(3901, 2796223), 24.0f);
        System.out.println("AnalogValueState is set.");

        // Update a few times
        for (int i = 0; i < 10; i++) {
            drv.update();
            System.out.println(drv.toString());
            Thread.sleep(1000);
        }

        drv.close();
    }

    public Double getAnalogInputState(BacNetObject oid) {
        return this.analogInputStates.get(oid);
    }

    public Double getAnalogValueState(BacNetObject oid) {
        return this.analogValueStates.get(oid);
    }

    public void setAnalogValueState(BacNetObject oid, float value) throws BACnetException {
        // Get extended information for all remote devices.
        for (RemoteDevice d : this.bacNetDevice.getRemoteDevices()) {
            //bacnetDevice.getExtendedDeviceInformation(d);

            if (oid.deviceId == d.getInstanceNumber()) {
                this.bacNetDevice.setPresentValue(d,
                        new ObjectIdentifier(ObjectType.analogValue, oid.objectId),
                        new com.serotonin.bacnet4j.type.primitive.Real(value));
            }
        }
    }

    public void init() throws IOException {
        this.bacNetDevice = new LocalDevice(1984, "255.255.255.255");
        this.bacNetDevice.getEventHandler().addListener(new MyDeviceEventListener());
        this.bacNetDevice.initialize();
        if (!this.STANDALONE) this.timeRegistry.subscribe(this, TimeSubscribeEnum.SECOND);
    }

    public void addDevice(String host, int port) throws OSHException {
        if (this.devices.add(host)) {
            try {
                this.discover(host, port);
            } catch (UnknownHostException | BACnetException e) {
                this.devices.remove(host);
                throw new OSHException("can't connect to host " + host, e);
            }
        }
    }

    public void discover(String controllerHostname, int port) throws BACnetException, UnknownHostException {
        InetSocketAddress addr =
                new InetSocketAddress(
                        InetAddress.getByName(controllerHostname), port);
        this.bacNetDevice.sendUnconfirmed(addr, null, new WhoIsRequest());

        // Wait a bit for responses to come in.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // nop.
        }
    }

    @SuppressWarnings("unchecked")
    public void update() throws BACnetException {
        // Get extended information for all remote devices.
        for (RemoteDevice d : this.bacNetDevice.getRemoteDevices()) {
            //bacnetDevice.getExtendedDeviceInformation(d);

            // instanceNumber of the current RemoteDevice d
            int deviceId = d.getInstanceNumber();


            List<ObjectIdentifier> oIds = null;
            try {
                oIds = ((SequenceOf<ObjectIdentifier>) this.bacNetDevice.sendReadPropertyAllowNull(d, d
                        .getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();
            } catch (BACnetTimeoutException te) {
                if (this.logger != null) {
                    this.logger.logError("" + te.getMessage());
                } else {
                    System.out.println("" + te.getMessage());
                }
            }

            if (oIds != null) {
                PropertyReferences refs = new PropertyReferences();
                for (ObjectIdentifier oid : oIds) {
                    if (ObjectType.analogInput.equals(oid.getObjectType())) {
                        refs.add(oid, PropertyIdentifier.presentValue);
                    } else if (ObjectType.analogValue.equals(oid.getObjectType())) {
                        refs.add(oid, PropertyIdentifier.presentValue);
                    }
                }

                PropertyValues pvs = this.bacNetDevice.readProperties(d, refs);
                for (ObjectPropertyReference opr : pvs) {
                    ObjectIdentifier oid = opr.getObjectIdentifier();
                    if (ObjectType.analogInput.equals(oid.getObjectType())) {
                        if (PropertyIdentifier.presentValue.equals(opr.getPropertyIdentifier())) {
                            Encodable value = pvs.getNoErrorCheck(opr);
                            this.analogInputStates.put(
                                    new BacNetObject(deviceId, oid
                                            .getInstanceNumber()), this.interpretDoubleValue(value));
                        }
                    }
                    if (ObjectType.analogValue.equals(oid.getObjectType())) {
                        if (PropertyIdentifier.presentValue.equals(opr.getPropertyIdentifier())) {
                            Encodable value = pvs.getNoErrorCheck(opr);
                            this.analogValueStates.put(
                                    new BacNetObject(deviceId, oid
                                            .getInstanceNumber()), this.interpretDoubleValue(value));
                        }
                    }
                }
            }

        }
    }

    private double interpretDoubleValue(Encodable value) {
        if (value instanceof com.serotonin.bacnet4j.type.primitive.Double) {
            return ((com.serotonin.bacnet4j.type.primitive.Double) value).doubleValue();
        }
        if (value instanceof com.serotonin.bacnet4j.type.primitive.Real) {
            return ((com.serotonin.bacnet4j.type.primitive.Real) value).floatValue();
        }
        if (value instanceof com.serotonin.bacnet4j.type.primitive.SignedInteger) {
            return ((com.serotonin.bacnet4j.type.primitive.SignedInteger) value).longValue();
        }
        if (value instanceof com.serotonin.bacnet4j.type.primitive.UnsignedInteger) {
            return ((com.serotonin.bacnet4j.type.primitive.UnsignedInteger) value).longValue();
        }

        return Double.NaN;
    }

    public void close() {
        this.bacNetDevice.terminate();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("AI { ");
        for (Entry<BacNetObject, Double> entry : this.analogInputStates.entrySet()) {
            str.append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        str.append(" } \n");
        str.append("AV { ");
        for (Entry<BacNetObject, Double> entry : this.analogValueStates.entrySet()) {
            str.append(entry.getKey()).append(" = ").append(entry.getValue()).append("    ");
        }
        str.append(" } ");

        return str.toString();
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        try {
            if (this.rediscover_countdown <= 0) {
                for (String dev : this.devices) {
                    try {
                        this.discover(dev, 47808);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException("unknown host: " + dev, e);
                    }
                }
                this.rediscover_countdown = this.REDISCOVER_INTERVAL;
            }
            this.rediscover_countdown--;
            this.update();
        } catch (BACnetException e) {
            throw new RuntimeException("internal bacnet error", e);
        }
    }

    static public class BacNetObject {
        public final int deviceId;
        public final int objectId;

        public BacNetObject(int deviceId, int objectId) {
            super();
            this.deviceId = deviceId;
            this.objectId = objectId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;

            final BacNetObject other = (BacNetObject) obj;

            return ((this.deviceId == other.deviceId) &&
                    (this.objectId == other.objectId));
        }

        @Override
        public int hashCode() {
            return this.objectId;
        }

        @Override
        public String toString() {
            return "[/" + this.deviceId + "/" + this.objectId + "]";
        }
    }

    private static class MyDeviceEventListener extends DefaultDeviceEventListener {
        @Override
        public void iAmReceived(RemoteDevice d) {
            System.out.println("IAm received" + d);
        }
    }
}
