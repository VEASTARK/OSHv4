package osh.datatypes.registry.details.utility;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.registry.StateExchange;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@XmlType
@Entity
@Table(name = "log_currentpricesignal")
public class CurrentPriceSignalLogDetails extends StateExchange {


    /**
     * SERIAL
     */
    private static final long serialVersionUID = -852140511093577391L;

    @Transient
    protected AncillaryCommodity ancillaryCommodityEnum;

    @Column(name = "c_ancillarycommodity")
    protected String ancillaryCommodity;

    @Column(name = "c_priceperunit")
    protected double pricePerUnit;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    public CurrentPriceSignalLogDetails() {
        this(null, -1L);
    }

    public CurrentPriceSignalLogDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }


    public String getAncillaryCommodity() {
        return this.ancillaryCommodity;
    }

    public void setAncillaryCommodity(String ancillaryCommodity) {
        this.ancillaryCommodityEnum = AncillaryCommodity.fromString(ancillaryCommodity);
        this.ancillaryCommodity = ancillaryCommodity;
    }

    public void setCommodity(AncillaryCommodity ancillaryCommodityEnum) {
        this.ancillaryCommodityEnum = ancillaryCommodityEnum;
        this.ancillaryCommodity = ancillaryCommodityEnum.getCommodity();
    }

    public double getPricePerUnit() {
        return this.pricePerUnit;
    }

    public void getPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }


}
