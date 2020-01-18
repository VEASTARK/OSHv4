package osh.driver.meter;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Ingo Mauser
 */
public class BcontrolHeaterData {

    @XmlElement(name = "mode")
    private String mode;

    @XmlElement(name = "order")
    private int order;

    @XmlElement(name = "timestamp")
    private String timestamp;

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "serial")
    private String serial;

    @XmlElement(name = "uuid")
    private String uuid;

    @XmlElement(name = "channel")
    private int channel;

    @XmlElement(name = "manufacturer")
    private String manufacturer;

    @XmlElement(name = "product")
    private String product;

    @XmlElement(name = "state")
    private String state;

    @XmlElement(name = "sw_version")
    private String sw_version;

    @XmlElement(name = "hw_version")
    private String hw_version;

    @XmlElement(name = "production_date")
    private String production_date;

    @XmlElement(name = "vendor")
    private String vendor;

    @XmlElement(name = "model")
    private String model;

    @XmlElement(name = "operating_hours")
    private int operating_hours;

    @XmlElement(name = "operating_seconds")
    private int operating_seconds;

    @XmlElement(name = "temperatur_boiler")
    private int temperatur_boiler;

    @XmlElement(name = "user_temperatur_nominal")
    private int user_temperatur_nominal;

    @XmlElement(name = "switches")
    private List<BcontrolHeaterDataSwitches> switches;

    @XmlElement(name = "errors")
    private List<BcontrolHeaterDataErrors> errors;

    @XmlElement(name = "registers")
    private List<BcontrolHeaterDataRegisters> registers;

    @XmlElement(name = "update")
    private BcontrolHeaterDataUpdate update;

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSerial() {
        return this.serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getChannel() {
        return this.channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSw_version() {
        return this.sw_version;
    }

    public void setSw_version(String sw_version) {
        this.sw_version = sw_version;
    }

    public String getHw_version() {
        return this.hw_version;
    }

    public void setHw_version(String hw_version) {
        this.hw_version = hw_version;
    }

    public String getProduction_date() {
        return this.production_date;
    }

    public void setProduction_date(String production_date) {
        this.production_date = production_date;
    }

    public String getVendor() {
        return this.vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getOperating_hours() {
        return this.operating_hours;
    }

    public void setOperating_hours(int operating_hours) {
        this.operating_hours = operating_hours;
    }

    public int getOperating_seconds() {
        return this.operating_seconds;
    }

    public void setOperating_seconds(int operating_seconds) {
        this.operating_seconds = operating_seconds;
    }

    public int getTemperatur_boiler() {
        return this.temperatur_boiler;
    }

    public void setTemperatur_boiler(int temperatur_boiler) {
        this.temperatur_boiler = temperatur_boiler;
    }

    public int getUser_temperatur_nominal() {
        return this.user_temperatur_nominal;
    }

    public void setUser_temperatur_nominal(int user_temperatur_nominal) {
        this.user_temperatur_nominal = user_temperatur_nominal;
    }

    public List<BcontrolHeaterDataSwitches> getSwitches() {
        return this.switches;
    }

    public void setSwitches(List<BcontrolHeaterDataSwitches> switches) {
        this.switches = switches;
    }

    public List<BcontrolHeaterDataErrors> getErrors() {
        return this.errors;
    }

    public void setErrors(List<BcontrolHeaterDataErrors> errors) {
        this.errors = errors;
    }

    public List<BcontrolHeaterDataRegisters> getRegisters() {
        return this.registers;
    }

    public void setRegisters(List<BcontrolHeaterDataRegisters> registers) {
        this.registers = registers;
    }

    public BcontrolHeaterDataUpdate getUpdate() {
        return this.update;
    }

    public void setUpdate(BcontrolHeaterDataUpdate update) {
        this.update = update;
    }

}
