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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A {@link Future} that allows for collecting data from a {@link Supplier}.
 *
 * <p>The {@code SamplingFuture} has the following semantic behavior:
 *
 * <p>Once created, a {@link Supplier} is called on a {@link ScheduledExecutorService} immediately.
 * The {@link Future} returned by the executor is stored. We call this data collection.
 *
 * <p>Each {@link Duration} interval after, we collect data.
 *
 * <p>If {@code get} is called, all data collected up to then is returned.
 *
 * <p>If {@code cancel} is called, no more data is collected and all future calls to {@code get}
 * will always return the same value.
 *
 * <p>{@code isCancelled} and {@code isDone} return if the future is not collecting (i.e. cancelled)
 */
public final class SamplingFuture<T> implements Future<List<T>> {
  /** Start a {@link SamplingFuture} that samples at a fixed millisecond period. */
  public static <T> SamplingFuture<T> startFixedPeriodCollectionMillis(
      Supplier<? extends T> source, int periodMillis, ScheduledExecutorService executor) {
    return new SamplingFuture<>(source, Duration.ofMillis(periodMillis), executor);
  }

  /** Start a {@link SamplingFuture} that samples at a fixed period from a {@link Duration}. */
  public static <T> SamplingFuture<T> startFixedPeriodCollection(
      Supplier<? extends T> source, Duration period, ScheduledExecutorService executor) {
    return new SamplingFuture<>(source, period, executor);
  }

  // we can make this support more complex schedules i think
  private final Duration period;
  private final ScheduledExecutorService executor;

  private final AtomicBoolean isCollecting = new AtomicBoolean(true);
  private final List<T> collectedData = new ArrayList<>();

  private List<Future<Optional<? extends T>>> dataFutures = new ArrayList<>();

  private SamplingFuture(
      Supplier<? extends T> source, Duration period, ScheduledExecutorService executor) {
    this.period = period;
    this.executor = executor;
    synchronized (dataFutures) {
      dataFutures.add(executor.submit(() -> collectDataAndReschedule(source)));
    }
  }

  /**
   * Stops collecting data. If {@code mayInterruptIfRunning} is true, the data is also pulled out.
   */
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    // this will kill all pending futures (and futures that get scheduled by {@code
    // collectDataAndReschedule}), so we don't need to cancel them
    isCollecting.set(false);
    if (mayInterruptIfRunning) {
      extractAllData();
    }
    return true;
  }

  /**
   * If we are collecting, try to extract values from all the {@link Futures} that are done and
   * filter them out of {@code dataFutures}. Otherwise, try to extract all the {@link Futures}.
   */
  @Override
  public List<T> get() {
    if (!isCollecting.get()) {
      extractAllData();
    } else {
      // pull out all futures that are done
      synchronized (collectedData) {
        dataFutures
            .stream()
            .filter(Future::isDone)
            .map(this::extractValue)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(collectedData::add);
        dataFutures = dataFutures.stream().filter(future -> !future.isDone()).collect(toList());
      }
    }

    return collectedData;
  }

  /**
   * If we are collecting, try to extract values from all the {@link Futures} within the timeout and
   * filter done futures out of {@code dataFutures}. Otherwise, try to extract all the {@link
   * Futures}.
   */
  // TODO(timur): this doesn't work yet; i left the code in but i think it will break
  @Override
  public List<T> get(long timeout, TimeUnit unit) {
    if (!isCollecting.get()) {
      extractAllData();
    } else {
      // pull out any futures that are currently running; they should be done by the end of this
      // block
      synchronized (dataFutures) {
        synchronized (collectedData) {
          dataFutures
              .stream()
              .map(future -> maybeExtractValue(future, timeout, unit))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .forEach(collectedData::add);
          dataFutures = dataFutures.stream().filter(future -> !future.isDone()).collect(toList());
        }
      }
    }

    return collectedData;
  }

  /** Returns if more data will be scheduled to be collected. */
  @Override
  public boolean isCancelled() {
    return !isCollecting.get() || executor.isShutdown();
  }

  /** Returns if any more data is being collected. */
  @Override
  public boolean isDone() {
    synchronized (dataFutures) {
      return isCancelled() && dataFutures.stream().allMatch(Future::isDone);
    }
  }

  /**
   * Collects from the {@link Supplier}, re-schedules for the next period start, and returns the
   * sample.
   */
  private Optional<? extends T> collectDataAndReschedule(Supplier<? extends T> source) {
    if (isCancelled()) {
      isCollecting.set(false);
      return Optional.empty();
    }

    // TODO(timur): need some sort of safety mechanism so this doesn't kill the chain on throw
    Instant start = Instant.now();
    T data = source.get();
    Duration rescheduleTime = period.minus(Duration.between(start, Instant.now()));

    if (!isCancelled()) {
      synchronized (dataFutures) {
        if (rescheduleTime.toNanos() > 0) {
          // if we have some extra time, schedule the next one in the future
          dataFutures.add(
              executor.schedule(
                  () -> collectDataAndReschedule(source), rescheduleTime.toNanos(), NANOSECONDS));
        } else {
          // if we don't, run the next one immediately
          dataFutures.add(executor.submit(() -> collectDataAndReschedule(source)));
        }
      }
    }
    return Optional.of(data);
  }

  /** Wait until {@link Futures} in {@code dataFutures} are done, then extract all the data. */
  private void extractAllData() {
    // TODO(timur): should i do this with lock.notify()?
    while (!isDone()) {
      try {
        Thread.sleep(period.toMillis());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    synchronized (collectedData) {
      dataFutures
          .stream()
          .map(this::extractValue)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(collectedData::add);
      dataFutures.clear();
    }
  }

  /**
   * Forcibly retrieve the value from a {@link Future}; if it can't be retrieved, then return an
   * empty {@link Optional}.
   */
  private Optional<? extends T> extractValue(Future<Optional<? extends T>> future) {
    try {
      return future.get();
    } catch (Exception e) {
      System.out.println("could not consume a future");
      e.printStackTrace();
    }
    return Optional.empty();
  }

  /**
   * Gracefully retrieve the value from a {@link Future}; if it can't be retrieved, then return an
   * empty {@link Optional}.
   */
  private Optional<? extends T> maybeExtractValue(
      Future<Optional<? extends T>> future, long timeout, TimeUnit unit) {
    try {
      return future.get(timeout, unit);
    } catch (Exception e) {
      System.out.println("could not consume a future");
      e.printStackTrace();
    }
    return Optional.empty();
  }
}
