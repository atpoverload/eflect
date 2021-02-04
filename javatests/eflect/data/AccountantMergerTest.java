package eflect.data;

import static org.junit.Assert.assertEquals;

import eflect.data.Accountant.Result;
import eflect.testing.data.FakeAccountant;
import eflect.testing.data.FakeSample;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AccountantMergerTest {
  private AccountantMerger merger;

  @Before
  public void setUp() {
    AtomicInteger counter = new AtomicInteger();
    merger =
        new AccountantMerger<Integer>(
            () -> {
              FakeAccountant<Integer> accountant = new FakeAccountant();
              accountant.setData(List.of(counter.getAndIncrement()));
              if (counter.get() < 5) {
                accountant.setResult(Result.UNACCOUNTABLE);
              } else {
                accountant.setResult(Result.ACCOUNTED);
              }
              return accountant;
            });
  }

  @After
  public void tearDown() {
    merger = null;
  }

  @Test
  public void process() {
    for (int i = 0; i < 5; i++) {
      Instant timestamp = Instant.EPOCH.plusMillis(i);
      merger.add(new FakeSample(timestamp, Result.ACCOUNTED, List.of(1)));
    }
    List<Integer> data = (List<Integer>) merger.process();
    assertEquals(5, data.size());
  }
}
