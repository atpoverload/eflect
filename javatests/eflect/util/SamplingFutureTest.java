package eflect.util;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SamplingFutureTest {
  // TODO(timur): make this 0 once we've added a barrier
  private static final int DURATION_MS = 50;
  private static final int SLEEP_TIME_MS = 5;

  private static final Supplier<?> LAMBDA_SOURCE = () -> "";
  private static final Supplier<?> METHOD_REF_SOURCE = String::new;
  private static final Supplier<?> SLOW_LAMBDA_SOURCE =
      () -> {
        try {
          Thread.sleep(2 * DURATION_MS);
        } catch (Exception e) {
          System.out.println("something bad happened in the slow test source");
          e.printStackTrace();
        }
        return LAMBDA_SOURCE.get();
      };

  private ScheduledExecutorService executor;

  @Before
  public void setUp() {
    // TODO(timur): we need to replace {@link Thread.sleep} with a barrier:
    //  //javatests/eflect/util:SamplingFutureTest         FLAKY, failed in 244 out of 1000 in 2.2s
    //  Stats over 1000 runs: max = 2.2s, min = 1.0s, avg = 1.6s, dev = 0.2s
    executor = newSingleThreadScheduledExecutor();
  }

  @After
  public void tearDown() {
    executor.shutdown();
  }

  @Test
  public void cancel_stopCollectionEventually() throws Exception {
    SamplingFuture<?> future =
        SamplingFuture.startFixedPeriodCollectionMillis(LAMBDA_SOURCE, DURATION_MS, executor);
    future.cancel(false);

    assertCancelledButCollecting(future);
    Thread.sleep(DURATION_MS);

    assertEmpty(future);
    assertNotCollecting(future);
  }

  @Test
  public void cancel_stopCollectionImmediately() throws Exception {
    SamplingFuture<?> future =
        SamplingFuture.startFixedPeriodCollectionMillis(LAMBDA_SOURCE, DURATION_MS, executor);
    future.cancel(true);

    assertNotCollecting(future);
    assertEmpty(future);
  }

  @Test
  public void get() throws Exception {
    SamplingFuture<?> future =
        SamplingFuture.startFixedPeriodCollectionMillis(LAMBDA_SOURCE, DURATION_MS, executor);

    assertCollecting(future);

    Thread.sleep(SLEEP_TIME_MS);

    assertContains(future, 1);
    assertCollecting(future);

    Thread.sleep(DURATION_MS);

    assertContains(future, 2);
    assertCollecting(future);

    Thread.sleep(8 * DURATION_MS);
    assertContains(future, 10);
    assertCollecting(future);
  }

  // TODO(timur): there is a race condition if {@link get} is spammed
  @Test
  public void get_withTimeout() throws Exception {
    SamplingFuture<?> future =
        SamplingFuture.startFixedPeriodCollectionMillis(LAMBDA_SOURCE, DURATION_MS, executor);

    assertEquals(future.get(DURATION_MS, TimeUnit.MILLISECONDS).size(), 0);
    Thread.sleep(SLEEP_TIME_MS);
    assertEquals(future.get(DURATION_MS, TimeUnit.MILLISECONDS).size(), 1);
    Thread.sleep(SLEEP_TIME_MS);
    assertEquals(future.get(DURATION_MS, TimeUnit.MILLISECONDS).size(), 2);
  }

  @Test
  public void cancel_get() throws Exception {
    SamplingFuture<?> future =
        SamplingFuture.startFixedPeriodCollectionMillis(LAMBDA_SOURCE, DURATION_MS, executor);

    assertCollecting(future);

    Thread.sleep(SLEEP_TIME_MS);
    future.cancel(true);

    assertContains(future, 1);
    assertNotCollecting(future);

    Thread.sleep(DURATION_MS);

    assertContains(future, 1);
    assertNotCollecting(future);
  }

  @Test
  public void cancel_get_noDeadlockOnSlowSource() throws Exception {
    SamplingFuture<?> future =
        SamplingFuture.startFixedPeriodCollectionMillis(SLOW_LAMBDA_SOURCE, DURATION_MS, executor);

    Thread.sleep(DURATION_MS);
    future.cancel(false);
    future.get();

    assertNotCollecting(future);
    assertContains(future, 1);
    assertNotCollecting(future);
  }

  @Test
  public void get_semanticallyIdenticalSources() throws Exception {
    SamplingFuture<?> future1 =
        SamplingFuture.startFixedPeriodCollectionMillis(LAMBDA_SOURCE, DURATION_MS, executor);
    SamplingFuture<?> future2 =
        SamplingFuture.startFixedPeriodCollectionMillis(METHOD_REF_SOURCE, DURATION_MS, executor);

    Thread.sleep(SLEEP_TIME_MS);

    assertEquals(
        String.format("expected %s and %s to be the same", future1.get(), future2.get()),
        future1.get(),
        future2.get());
  }

  private void assertCollecting(SamplingFuture<?> future) {
    assertFalse("expected future to not be cancelled", future.isCancelled());
    assertFalse("expected future to not be done", future.isDone());
  }

  private void assertCancelledButCollecting(SamplingFuture<?> future) {
    assertTrue("expected future to be cancelled", future.isCancelled());
    assertFalse("expected future to not be done", future.isDone());
  }

  private void assertNotCollecting(SamplingFuture<?> future) {
    assertTrue("expected future to be cancelled", future.isCancelled());
    assertTrue("expected future to be done", future.isDone());
  }

  private void assertContains(SamplingFuture<?> future, int count) {
    assertContains(future, createSourcesList(LAMBDA_SOURCE, count));
  }

  private void assertContains(SamplingFuture<?> future, List<?> items) {
    List<?> data = future.get();
    assertEquals(
        String.format("expected %d items but found %d", items.size(), data.size()),
        data.size(),
        items.size());
    for (int i = 0; i < items.size(); i++) {
      assertEquals(
          String.format("expected %s but got %s", items.get(i), data.get(i)),
          data.get(i),
          items.get(i));
    }
  }

  private void assertEmpty(SamplingFuture<?> future) {
    int size = future.get().size();
    assertEquals(String.format("expected empty but was %d", size), size, 0);
  }

  private static <T> List<T> createSourcesList(Supplier<T> source, int count) {
    List<T> data = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      data.add(source.get());
    }
    return data;
  }
}
