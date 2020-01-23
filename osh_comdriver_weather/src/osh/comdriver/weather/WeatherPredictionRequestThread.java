package osh.comdriver.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import osh.comdriver.WeatherPredictionProviderComDriver;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.core.logging.IGlobalLogger;
import osh.openweathermap.prediction.PredictedWeatherMap;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Jan Mueller, Ingo Mauser
 */
public class WeatherPredictionRequestThread implements Runnable {

    private final IGlobalLogger globalLogger;
    private final WeatherPredictionProviderComDriver comDriver;
    private final String urlWeatherPrediction;
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
     * @param weatherPredictionProviderComDriver
     * @param urlWeatherPrediction
     * @param apiKey
     */
    public WeatherPredictionRequestThread(
            IGlobalLogger globalLogger,
            WeatherPredictionProviderComDriver weatherPredictionProviderComDriver,
            String urlWeatherPrediction,
            String apiKey) {
        this.globalLogger = globalLogger;
        this.comDriver = weatherPredictionProviderComDriver;
        this.urlWeatherPrediction = urlWeatherPrediction;
        this.apiKey = apiKey;
    }


    @Override
    public void run() {
        while (!this.shutdown) {

            if (this.comDriver.getTimer().getUnixTime() - this.lastLog >= this.logEverySeconds) {
                try {
                    // get WeatherPredictionn
                    WeatherPredictionDetails weatherDetails = new WeatherPredictionDetails(
                            this.comDriver.getUUID(),
                            this.comDriver.getTimer().getUnixTime(),
                            this.getWeatherPrediction(this.urlWeatherPrediction, this.apiKey));

                    // send WeatherPrediction to ComDriver
                    this.comDriver.receivePredictionDetails(weatherDetails);

                    this.lastLog = this.comDriver.getTimer().getUnixTime();
                } catch (Exception e) {
                    this.globalLogger.logError("Reading weather prediction info failed", e);

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


    public PredictedWeatherMap getWeatherPrediction(String urlToWeatherAPI, String apiKey) {

        ObjectMapper mapper = new ObjectMapper();
        PredictedWeatherMap obj = null;

        try {
            obj = mapper.readValue(new URL(urlToWeatherAPI + apiKey), PredictedWeatherMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

}
