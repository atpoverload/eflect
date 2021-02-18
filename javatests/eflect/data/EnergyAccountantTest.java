package eflect.data;

import static org.junit.Assert.assertEquals;

import eflect.data.Accountant.Result;
import eflect.testing.data.FakeAccountant;
import java.time.Instant;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// TODO(timur): add tests for discard
public class EnergyAccountantTest {
  private FakeAccountant<ThreadActivity> activityAccountant;
  private EnergyAccountant accountant;

  @Before
  public void setUp() {
    activityAccountant = new FakeAccountant<>();
    accountant = new EnergyAccountant(1, 1, 0, activityAccountant);
    activityAccountant.setResult(Result.UNACCOUNTABLE);
  }

  @After
  public void tearDown() {
    accountant = null;
    activityAccountant = null;
  }

  @Test
  public void account_noData() {
    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_notEnoughData() {
    accountant.add(new EnergySample(Instant.now(), new double[][] {{0.0}}));
    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_enoughData() {
    accountant.add(new EnergySample(Instant.EPOCH, new double[][] {{0.0}}));
    accountant.add(new EnergySample(Instant.MAX, new double[][] {{1.0}}));
    activityAccountant.setResult(Result.ACCOUNTED);
    assertEquals(Accountant.Result.ACCOUNTED, accountant.account());
  }

  @Test
  public void process_enoughData_noTasks() {
    accountant.add(new EnergySample(Instant.EPOCH, new double[][] {{0.0}}));
    accountant.add(new EnergySample(Instant.MAX, new double[][] {{1.0}}));
    assertEquals(0, accountant.process().size());
  }

  @Test
  public void process_enoughData_oneTask() {
    accountant.add(new EnergySample(Instant.EPOCH, new double[][] {{0.0}}));
    accountant.add(new EnergySample(Instant.MAX, new double[][] {{1.0}}));
    activityAccountant.setResult(Result.ACCOUNTED);
    activityAccountant.setData(List.of(new ThreadActivity(0, "fake-thread", 0, 1)));
    List<EnergyFootprint> footprints = (List<EnergyFootprint>) accountant.process();
    assertEquals(1, footprints.get(0).energy, 0);
  }

  @Test
  public void process_enoughData_twoTasks() {
    accountant.add(new EnergySample(Instant.EPOCH, new double[][] {{0.0}}));
    accountant.add(new EnergySample(Instant.MAX, new double[][] {{2.0}}));
    activityAccountant.setResult(Result.ACCOUNTED);
    activityAccountant.setData(
        List.of(
            new ThreadActivity(0, "fake-thread-1", 0, 0.5),
            new ThreadActivity(1, "fake-thread-2", 0, 0.5)));
    List<EnergyFootprint> footprints = (List<EnergyFootprint>) accountant.process();
    assertEquals(1, footprints.get(0).energy, 0);
    assertEquals(1, footprints.get(1).energy, 0);
  }
}
