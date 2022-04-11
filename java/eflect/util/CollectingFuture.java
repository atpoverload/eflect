package eflect.util;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Future} that allows for fixed periodic collection from a {@link Supplier}.
 *
 * <p>The {@code CollectingFuture} has the following semantic behavior:
 *
 * <p>Once created, a {@link Supplier} is called on a {@link ScheduledExecutorService} immediately.
 * We'll call this data collection.
 *
 * <p>Each {@link Duration} interval after, we collect another piece of data.
 *
 * <p>If either {@code get} or {@code cancel} is called, no more data is collected and all future
 * calls to {@code get} will always return the same value. In addition, {@code isDone} will be true.
 *
 * <p>{@code isCancelled} is only true if {@code cancel} was called before {@code get}.
 */
public final class CollectingFuture<T> implements Future<List<? extends T>> {
  /** Helper to create a new {@code CollectingFuture}. */
  public static <T> CollectingFuture<T> create(
      Supplier<? extends T> source, Duration period, ScheduledExecutorService executor) {
    return new CollectingFuture<T>(source, period, executor);
  }

  private final ArrayList<Future<Optional<? extends T>>> dataFutures = new ArrayList<>();

  private boolean isCollecting = true;
  private boolean isCancelled = false;
  private List<? extends T> data = null;

  private CollectingFuture(
      Supplier<? extends T> source, Duration period, ScheduledExecutorService executor) {
    synchronized (dataFutures) {
      dataFutures.add(executor.submit(() -> collectDataAndReschedule(source, period, executor)));
    }
  }

  /** Stops collecting data. */
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (isCollecting) {
      isCancelled = true;
    }
    isCollecting = false;
    return true;
  }

  /** Stops collecting and returns the cached data. */
  @Override
  public List<? extends T> get() {
    return stopAndGetData(CollectingFuture::getFuture);
  }

  /** Stops collecting and returns the cached data. */
  @Override
  public List<? extends T> get(long timeout, TimeUnit unit) {
    return stopAndGetData(future -> CollectingFuture.getFuture(future, timeout, unit));
  }

  /** Returns if {@code cancel} was called before {@code get}. */
  @Override
  public boolean isCancelled() {
    return isCancelled;
  }

  /** Returns if we are no longer collecting. */
  @Override
  public boolean isDone() {
    return !isCollecting;
  }

  /** Collects from the source and then re-schedules for the next period start. */
  private Optional<? extends T> collectDataAndReschedule(
      Supplier<? extends T> source, Duration period, ScheduledExecutorService executor) {
    if (!isCollecting || isCancelled || executor.isShutdown()) {
      return Optional.empty();
    }

    // TODO(timur): need some sort of safety mechanism so this doesn't kill the thread
    Instant start = Instant.now();
    T data = source.get();
    Duration rescheduleTime = period.minus(Duration.between(start, Instant.now()));

    synchronized (dataFutures) {
      if (rescheduleTime.toNanos() > 0) {
        // if we have some extra time, schedule the next one in the future
        dataFutures.add(
            executor.schedule(
                () -> collectDataAndReschedule(source, period, executor),
                rescheduleTime.toNanos(),
                NANOSECONDS));
      } else {
        // if we don't, run the next one immediately
        dataFutures.add(executor.submit(() -> collectDataAndReschedule(source, period, executor)));
      }
    }
    return Optional.of(data);
  }

  private static <T> Optional<? extends T> getFuture(Future<Optional<? extends T>> future) {
    try {
      if (future.isDone()) {
        return future.get();
      } else {
        future.cancel(true);
      }
    } catch (Exception e) {
      System.out.println("could not consume a future");
      e.printStackTrace();
    }
    return Optional.empty();
  }

  private static <T> Optional<? extends T> getFuture(
      Future<Optional<? extends T>> future, long timeout, TimeUnit unit) {
    try {
      if (future.isDone()) {
        return future.get(timeout, unit);
      } else {
        future.cancel(true);
      }
    } catch (Exception e) {
      System.out.println("could not consume a future");
      e.printStackTrace();
    }
    return Optional.empty();
  }

  /** Stops collecting data and safely pulls out as much as we can. */
  private synchronized List<? extends T> stopAndGetData(
      Function<Future<Optional<? extends T>>, Optional<? extends T>> func) {
    isCollecting = false;
    if (data == null) {
      synchronized (dataFutures) {
        data =
            dataFutures
                .stream()
                .map(func)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        dataFutures.clear();
      }
    }
    return data;
  }
}
