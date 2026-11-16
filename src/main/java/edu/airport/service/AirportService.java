package edu.airport.service;

import edu.airport.config.PlaneConfig;
import edu.airport.exception.AirportException;
import edu.airport.model.AirportFacilities;
import edu.airport.model.Plane;
import edu.airport.model.PlaneReport;
import edu.airport.model.TerminalSnapshot;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class AirportService {

  private static final Logger LOGGER = LogManager.getLogger(AirportService.class);
  private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

  private final AirportFacilities facilities;

  public AirportService(AirportFacilities facilities) {
    if (facilities == null) {
      throw new IllegalArgumentException("Airport facilities must not be null");
    }
    this.facilities = facilities;
  }

  public ServiceReport serve(List<PlaneConfig> schedules) throws AirportException {
    if (schedules == null || schedules.isEmpty()) {
      throw new IllegalArgumentException("At least one flight must be scheduled");
    }

    List<Plane> planes = createPlanes(schedules);
    TerminalSnapshot before = facilities.getTerminal().snapshot();
    long startedAt = System.nanoTime();
    LOGGER.info("Serving {} flights with {} gates", planes.size(), facilities.getGatePool()
        .getGateCount());

    List<PlaneReport> reports = new ArrayList<>(planes.size());
    ExecutorService executor = Executors.newFixedThreadPool(planes.size(),
        new PlaneThreadFactory());
    try {
      List<Future<PlaneReport>> futures = executor.invokeAll(planes);
      for (Future<PlaneReport> future : futures) {
        reports.add(future.get());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
      throw new AirportException("Airport service was interrupted", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause() == null ? e : e.getCause();
      throw new AirportException("Flight service failed: " + cause.getMessage(), cause);
    } finally {
      shutdown(executor);
    }

    long durationMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    TerminalSnapshot after = facilities.getTerminal().snapshot();
    LOGGER.info("All {} flights served in {} ms; terminal {}/{}, gates free {}/{}",
        reports.size(), durationMillis, after.passengers(), after.capacity(),
        facilities.getGatePool().getAvailableGateCount(),
        facilities.getGatePool().getGateCount());
    return new ServiceReport(facilities.getName(), facilities.getGatePool().getGateCount(),
        before, after, reports, durationMillis);
  }

  private List<Plane> createPlanes(List<PlaneConfig> schedules) {
    List<Plane> planes = new ArrayList<>(schedules.size());
    for (PlaneConfig schedule : schedules) {
      if (schedule == null) {
        throw new IllegalArgumentException("Flight list must not contain null entries");
      }
      planes.add(new Plane(schedule, facilities));
    }
    return planes;
  }

  private void shutdown(ExecutorService executor) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LOGGER.warn("Plane workers did not stop in {} s, forcing shutdown",
            SHUTDOWN_TIMEOUT_SECONDS);
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
      LOGGER.warn("Interrupted while waiting for plane workers to stop");
    }
  }

  private static final class PlaneThreadFactory implements ThreadFactory {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public Thread newThread(Runnable task) {
      Thread thread = new Thread(task, "plane-worker-" + counter.incrementAndGet());
      thread.setDaemon(false);
      return thread;
    }
  }
}
