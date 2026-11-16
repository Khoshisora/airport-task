package edu.airport.io;

import edu.airport.config.AirportConfig;
import edu.airport.config.PlaneConfig;
import edu.airport.exception.AirportException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class AirportConfigReader {

  private static final Logger LOGGER = LogManager.getLogger(AirportConfigReader.class);
  private static final String FIELD_SEPARATOR = "\\s*\\|\\s*";
  private static final int AIRPORT_FIELD_COUNT = 4;
  private static final int PLANE_FIELD_COUNT = 5;

  public AirportConfig read(Path path) throws AirportException {
    if (path == null) {
      throw new AirportException("Airport data file path must not be null");
    }
    LOGGER.info("Reading airport data from file {}", path);
    try {
      List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      return parse(lines, path.toString());
    } catch (IOException e) {
      throw new AirportException("Cannot read airport data file: " + path, e);
    }
  }

  public AirportConfig readResource(String resourceName) throws AirportException {
    if (resourceName == null || resourceName.isBlank()) {
      throw new AirportException("Airport data resource name must not be blank");
    }
    LOGGER.info("Reading airport data from classpath resource {}", resourceName);
    try (InputStream stream = AirportConfigReader.class.getClassLoader()
        .getResourceAsStream(resourceName)) {
      if (stream == null) {
        throw new AirportException("Classpath resource not found: " + resourceName);
      }
      String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      return parse(content.lines().toList(), "classpath:" + resourceName);
    } catch (IOException e) {
      throw new AirportException("Cannot read classpath resource: " + resourceName, e);
    }
  }

  private AirportConfig parse(List<String> lines, String source) throws AirportException {
    List<NumberedLine> dataLines = collectDataLines(lines);
    if (dataLines.isEmpty()) {
      throw new AirportException("Airport data is empty: " + source);
    }

    AirportHeader airport = parseAirport(dataLines.get(0), source);

    List<PlaneConfig> planes = new ArrayList<>(dataLines.size() - 1);
    for (int index = 1; index < dataLines.size(); index++) {
      NumberedLine line = dataLines.get(index);
      planes.add(parsePlane(line, source));
    }

    try {
      AirportConfig config = new AirportConfig(
          airport.name(),
          airport.gateCount(),
          airport.terminalCapacity(),
          airport.initialTerminalPassengers(),
          planes);
      LOGGER.info("Loaded airport {}: {} gates, terminal capacity {}, {} flights",
          config.name(), config.gateCount(), config.terminalCapacity(), config.planes().size());
      return config;
    } catch (IllegalArgumentException e) {
      throw new AirportException("Invalid airport data in " + source + ": " + e.getMessage(), e);
    }
  }

  private List<NumberedLine> collectDataLines(List<String> lines) {
    List<NumberedLine> dataLines = new ArrayList<>();
    for (int index = 0; index < lines.size(); index++) {
      String line = lines.get(index).strip();
      if (!line.isEmpty() && !line.startsWith("#")) {
        dataLines.add(new NumberedLine(index + 1, line));
      }
    }
    return dataLines;
  }

  private AirportHeader parseAirport(NumberedLine line, String source) throws AirportException {
    String[] fields = split(line, AIRPORT_FIELD_COUNT, "airport", source);
    String name = fields[0].strip();
    if (name.isEmpty()) {
      throw new AirportException("Invalid airport name at " + source + ":" + line.number()
          + ": name must not be blank");
    }
    int gateCount = parsePositiveInt(fields[1], "gate count", line, source);
    int terminalCapacity = parsePositiveInt(fields[2], "terminal capacity", line, source);
    int waitingPassengers = parseInt(fields[3], "initial terminal passengers", line, source);
    if (waitingPassengers < 0 || waitingPassengers > terminalCapacity) {
      throw new AirportException("Invalid initial terminal passengers at "
          + source + ":" + line.number() + ": value must be between 0 and terminal capacity");
    }
    return new AirportHeader(name, gateCount, terminalCapacity, waitingPassengers);
  }

  private PlaneConfig parsePlane(NumberedLine line, String source) throws AirportException {
    String[] fields = split(line, PLANE_FIELD_COUNT, "flight", source);
    try {
      return new PlaneConfig(
          fields[0].strip(),
          parsePositiveInt(fields[1], "flight capacity", line, source),
          parseInt(fields[2], "passengers on board", line, source),
          parseInt(fields[3], "passengers to board", line, source),
          parseLong(fields[4], "service delay", line, source));
    } catch (IllegalArgumentException e) {
      throw new AirportException(invalidLineMessage(line, source, e.getMessage()), e);
    }
  }

  private String[] split(NumberedLine line, int expectedFields, String entity, String source)
      throws AirportException {
    String[] fields = line.text().split(FIELD_SEPARATOR, -1);
    if (fields.length != expectedFields) {
      throw new AirportException(
          "Invalid " + entity + " line at " + source + ":" + line.number()
              + ": expected " + expectedFields + " fields separated by '|', got "
              + fields.length);
    }
    return fields;
  }

  private int parsePositiveInt(String value, String field, NumberedLine line, String source)
      throws AirportException {
    int parsed = parseInt(value, field, line, source);
    if (parsed <= 0) {
      throw new AirportException("Invalid " + field + " at " + source + ":" + line.number()
          + ": value must be positive");
    }
    return parsed;
  }

  private int parseInt(String value, String field, NumberedLine line, String source)
      throws AirportException {
    try {
      return Integer.parseInt(value.strip());
    } catch (NumberFormatException e) {
      throw new AirportException("Invalid " + field + " at " + source + ":" + line.number()
          + ": '" + value.strip() + "' is not an integer", e);
    }
  }

  private long parseLong(String value, String field, NumberedLine line, String source)
      throws AirportException {
    try {
      return Long.parseLong(value.strip());
    } catch (NumberFormatException e) {
      throw new AirportException("Invalid " + field + " at " + source + ":" + line.number()
          + ": '" + value.strip() + "' is not an integer", e);
    }
  }

  private String invalidLineMessage(NumberedLine line, String source, String reason) {
    return "Invalid data at " + source + ":" + line.number() + ": " + reason;
  }

  private record NumberedLine(int number, String text) {
  }

  private record AirportHeader(
      String name,
      int gateCount,
      int terminalCapacity,
      int initialTerminalPassengers) {
  }
}
