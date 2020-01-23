package osh.comdriver.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import osh.comdriver.WeatherPredictionProviderComDriver;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.core.logging.IGlobalLogger;
import osh.openweathermap.current.CurrentWeatherMap;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Jan Mueller
 */
public class CurrentWeatherRequestThread implements Runnable {

    private final IGlobalLogger globalLogger;
    private final WeatherPredictionProviderComDriver comDriver;
    private final String urlCurrentWeather;
    private final String apiKey;
    private final int logEverySeconds = 60;
    private boolean shutdown;
    private LocalDateTime lastException = LocalDateTime.now();
    private int reconnectWait;
    private long lastLog;


    /**
     * CONSTRUCTOR
     *
     * @param globalLogger
     * @param comDriver
     * @param urlCurrentWeather
     * @param apiKey
     */
    public CurrentWeatherRequestThread(IGlobalLogger globalLogger,
                                       WeatherPredictionProviderComDriver comDriver, String urlCurrentWeather,
                                       String apiKey) {
        this.globalLogger = globalLogger;
        this.comDriver = comDriver;
        this.urlCurrentWeather = urlCurrentWeather;
        this.apiKey = apiKey;
    }

    @Override
    public void run() {
        while (!this.shutdown) {

            if (this.comDriver.getTimeDriver().getUnixTime() - this.lastLog >= this.logEverySeconds) {
                try {
                    // get and send to driver
                    CurrentWeatherDetails currentWeatherDetails = new CurrentWeatherDetails(
                            this.comDriver.getUUID(),
                            this.comDriver.getTimeDriver().getUnixTime(),
                            this.getCurrentWeather(this.urlCurrentWeather, this.apiKey));
                    this.comDriver.receiveCurrentDetails(currentWeatherDetails);

                    this.lastLog = this.comDriver.getTimeDriver().getUnixTime();
                } catch (Exception e) {
                    this.globalLogger.logError("Reading current weather info failed", e);

                    long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                            - this.lastException.toEpochSecond(ZoneOffset.UTC);
                    if (diff < 0 || diff > 300000) {
                        this.reconnectWait = 0;
                    } else {
                        if (this.reconnectWait <= 0) {
                            this.reconnectWait = 1;
                        }

                        this.reconnectWait *= 2;
                        if (this.reconnectWait > 180) {
                            this.reconnectWait = 180;
                        }
                    }
                    this.lastException = LocalDateTime.now();

                    try {
                        Thread.sleep(this.reconnectWait * 1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }

    }

    public void shutdown() {
        this.shutdown = true;
    }

    public CurrentWeatherMap getCurrentWeather(String urlToWeatherAPI, String apiKey) {

        ObjectMapper mapper = new ObjectMapper();
        CurrentWeatherMap obj = null;

        try {
            obj = mapper.readValue(new URL(urlToWeatherAPI + apiKey), CurrentWeatherMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
