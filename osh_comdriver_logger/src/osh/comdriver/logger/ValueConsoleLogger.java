package osh.comdriver.logger;

/**
 * @author Ingo Mauser
 */
public class ValueConsoleLogger extends ValueLogger {
    private String consoleLoggerPrefix = "comdriver-logger: ";

    @Override
    public void log(long timestamp, Object ex) {
//		if (ex instanceof ChilliiBusDriverRawDetails) {
//			ChilliiBusDriverRawDetails cd = (ChilliiBusDriverRawDetails) ex;
//			System.out.println("[" + timestamp + "] " + consoleLoggerPrefix + cd.toString());
//		}
//		if (ex instanceof HagerGatewayBusDriverDetails) {
//			HagerGatewayBusDriverDetails cd = (HagerGatewayBusDriverDetails) ex;
//			System.out.println("[" + timestamp + "] " + consoleLoggerPrefix + cd.toString());
//		}
    }

}
