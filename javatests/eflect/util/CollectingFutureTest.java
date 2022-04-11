package eflect.util;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CollectingFutureTest {
  private static final int DURATION_MS = 10;
  private static final int SLEEP_TIME_MS = DURATION_MS / 2;
  private static final Duration DURATION = Duration.ofMillis(DURATION_MS);
  private static final Supplier<?> LAMBDA_SOURCE = () -> "";
  private static final Supplier<?> METHOD_REF_SOURCE = String::new;

  private ScheduledExecutorService executor;

  @Before
  public void setUp() {
    // TODO(timur): we need a barrier mechanism; this is flaky:
    //   javatests/eflect/util:CollectingFutureTest - FLAKY, failed in 548 out of 1000 in 1.5s
    //   Stats over 1000 runs: max = 1.7s, min = 0.7s, avg = 1.1s, dev = 0.1s
    executor = newSingleThreadScheduledExecutor();
  }

  @After
  public void tearDown() {
    executor.shutdown();
  }

  // TODO(timur): we have no tests for the timeout impl but they are created by the executor
  @Test
  public void get_singleCollection() throws Exception {
    CollectingFuture<?> future = CollectingFuture.create(LAMBDA_SOURCE, DURATION, executor);

    assertFalse(future.isCancelled());
    assertFalse(future.isDone());

    Thread.sleep(SLEEP_TIME_MS);
    future.get();

    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
    assertEquals(future.get().size(), 1);
    assertEquals(future.get().get(0), LAMBDA_SOURCE.get());

    Thread.sleep(DURATION_MS);

    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
    assertEquals(future.get().size(), 1);
    assertEquals(future.get().get(0), LAMBDA_SOURCE.get());
  }

  @Test
  public void get_multipleCollections() throws Exception {
    int sampleCount = 10;
    CollectingFuture<?> future = CollectingFuture.create(LAMBDA_SOURCE, DURATION, executor);

    Thread.sleep(sampleCount * DURATION_MS);
    future.get();

    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
    assertTrue(sampleCount - 1 <= future.get().size());
    assertTrue(future.get().size() <= sampleCount + 1);
  }

  @Test
  public void get_semanticallyIdenticalSources() throws Exception {
    CollectingFuture<?> future1 = CollectingFuture.create(LAMBDA_SOURCE, DURATION, executor);
    CollectingFuture<?> future2 = CollectingFuture.create(METHOD_REF_SOURCE, DURATION, executor);

    Thread.sleep(SLEEP_TIME_MS);

    assertEquals(future1.get(), future2.get());
  }

  // TODO(timur): cancel is identical for both true and false
  @Test
  public void cancel_stopsCollection1() throws Exception {
    CollectingFuture<?> future = CollectingFuture.create(LAMBDA_SOURCE, DURATION, executor);

    future.cancel(false);

    assertTrue(future.isCancelled());
    assertTrue(future.isDone());
    assertEquals(future.get().size(), 0);

    Thread.sleep(DURATION_MS);

    assertTrue(future.isCancelled());
    assertTrue(future.isDone());
    assertEquals(future.get().size(), 0);
  }

  @Test
  public void cancel_stopsCollection2() throws Exception {
    CollectingFuture<?> future = CollectingFuture.create(LAMBDA_SOURCE, DURATION, executor);

    Thread.sleep(SLEEP_TIME_MS);
    future.cancel(false);

    assertTrue(future.isCancelled());
    assertTrue(future.isDone());
    assertEquals(future.get().size(), 1);
  }

  @Test
  public void get_cancel_notCancelled() throws Exception {
    CollectingFuture<?> future = CollectingFuture.create(LAMBDA_SOURCE, DURATION, executor);

    Thread.sleep(SLEEP_TIME_MS);
    future.get();
    future.cancel(false);

    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
  }
}
