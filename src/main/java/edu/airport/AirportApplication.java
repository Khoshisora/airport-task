package edu.airport;

import edu.airport.config.AirportConfig;
import edu.airport.exception.AirportException;
import edu.airport.io.AirportConfigReader;
import edu.airport.model.Airport;
import edu.airport.model.PlaneReport;
import edu.airport.model.TerminalSnapshot;
import edu.airport.service.AirportService;
import edu.airport.service.ServiceReport;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AirportApplication {

  private static final Logger LOGGER = LogManager.getLogger(AirportApplication.class);
  private static final String DEFAULT_DATA_RESOURCE = "airport-data.txt";

  private AirportApplication() {
  }

  public static void main(String[] args) {
    LOGGER.info("Airport application started");
    try {
      AirportConfig config = readConfig(args);
      Airport airport = Airport.getInstance(config);
      ServiceReport report = new AirportService(airport.getFacilities()).serve(config.planes());
      printReport(report);
      LOGGER.info("Airport application finished");
    } catch (AirportException | InvalidPathException e) {
      LOGGER.error("Application failed: {}", e.getMessage(), e);
      System.err.println("Application failed: " + e.getMessage());
    }
  }

  private static AirportConfig readConfig(String[] args) throws AirportException {
    AirportConfigReader reader = new AirportConfigReader();
    if (args.length > 0 && !args[0].isBlank()) {
      return reader.read(Path.of(args[0].strip()));
    }
    return reader.readResource(DEFAULT_DATA_RESOURCE);
  }

  private static void printReport(ServiceReport report) {
    TerminalSnapshot before = report.terminalBefore();
    TerminalSnapshot after = report.terminalAfter();

    System.out.printf("Airport: %s | gates: %d%n", report.airportName(), report.gateCount());
    System.out.printf("Terminal before: %d/%d passengers%n%n",
        before.passengers(), before.capacity());
    System.out.println("Served flights:");
    for (PlaneReport flight : report.flights()) {
      System.out.printf("  %-8s gate %-2d  disembarked %3d  boarded %3d  %5d ms%n",
          flight.flightId(), flight.gateNumber(), flight.disembarkedPassengers(),
          flight.boardedPassengers(), flight.serviceMillis());
    }
    System.out.printf("%nTerminal after: %d/%d passengers%n",
        after.passengers(), after.capacity());
    System.out.printf("All %d flights served in %d ms%n",
        report.flights().size(), report.durationMillis());
  }
}
