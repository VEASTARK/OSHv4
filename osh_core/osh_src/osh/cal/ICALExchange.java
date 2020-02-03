package osh.cal;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public interface ICALExchange {

    UUID getDeviceID();

    ZonedDateTime getTimestamp();
}
