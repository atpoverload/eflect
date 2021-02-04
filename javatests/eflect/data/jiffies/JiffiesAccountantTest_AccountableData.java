package eflect.data.jiffies;

import static org.junit.Assert.assertEquals;

import eflect.data.Accountant;
import eflect.data.ThreadActivity;
import eflect.testing.data.jiffies.FakeProcStat;
import eflect.testing.data.jiffies.FakeProcTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// TODO(timur): add tests for discard
public class JiffiesAccountantTest_AccountableData {
  private FakeProcStat procStat;
  private FakeProcTask procTask;
  private JiffiesAccountant accountant;

  @Before
  public void setUp() {
    procStat = new FakeProcStat();
    procTask = new FakeProcTask();
    accountant = new JiffiesAccountant(1, cpu -> 0);
  }

  @After
  public void tearDown() {
    accountant = null;
    procStat = null;
    procTask = null;
  }

  @Test
  public void account_singleTask_overaccounted() {
    procTask.addTask(0, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 1);
    procTask.reset();
    procTask.addTask(0, 0, 2);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.OVERACCOUNTED, accountant.account());
    for (ThreadActivity thread : accountant.process()) {
      assertEquals(1.0, thread.activity, 0.0);
    }
  }

  @Test
  public void account_twoTasks_overaccounted() {
    procTask.addTask(0, 0, 0);
    procTask.addTask(1, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 1);
    procTask.reset();
    procTask.addTask(0, 0, 1);
    procTask.addTask(1, 0, 2);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.OVERACCOUNTED, accountant.account());
    double[] activity = new double[] {1.0 / 3.0, 2.0 / 3.0};
    for (ThreadActivity thread : accountant.process()) {
      assertEquals(activity[(int) thread.id], thread.activity, 0.0);
    }
  }

  @Test
  public void account_threeTasks_overaccounted() {
    procTask.addTask(0, 0, 0);
    procTask.addTask(1, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 1);
    procTask.reset();
    procTask.addTask(0, 0, 1);
    procTask.addTask(1, 0, 2);
    procTask.addTask(2, 0, 1);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.OVERACCOUNTED, accountant.account());
    double[] activity = new double[] {1.0 / 3.0, 2.0 / 3.0, 0.0};
    for (ThreadActivity thread : accountant.process()) {
      assertEquals(activity[(int) thread.id], thread.activity, 0.0);
    }
  }

  @Test
  public void account_oneTask_accounted() {
    procTask.addTask(0, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 1);
    procTask.reset();
    procTask.addTask(0, 0, 1);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.ACCOUNTED, accountant.account());
  }

  @Test
  public void account_twoTasks_accounted() {
    procTask.addTask(0, 0, 0);
    procTask.addTask(1, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 4);
    procTask.reset();
    procTask.addTask(0, 0, 1);
    procTask.addTask(1, 0, 2);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.ACCOUNTED, accountant.account());
    double[] activity = new double[] {1.0 / 4.0, 2.0 / 4.0};
    for (ThreadActivity thread : accountant.process()) {
      assertEquals(activity[(int) thread.id], thread.activity, 0.0);
    }
  }

  @Test
  public void account_threeTasks_accounted() {
    procTask.addTask(0, 0, 0);
    procTask.addTask(1, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 1);
    accountant.add(procStat.read());

    procStat.setJiffies(0, 4);
    procTask.reset();
    procTask.addTask(0, 0, 1);
    procTask.addTask(1, 0, 2);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.ACCOUNTED, accountant.account());
    double[] activity = new double[] {1.0 / 4.0, 2.0 / 4.0};
    for (ThreadActivity thread : accountant.process()) {
      assertEquals(activity[(int) thread.id], thread.activity, 0.0);
    }
  }

  @Test
  public void add_accounted() {
    JiffiesAccountant otherAccountant = new JiffiesAccountant(1, cpu -> 0);

    procTask.addTask(0, 0, 0);
    procTask.addTask(1, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 4);
    procTask.reset();
    procTask.addTask(0, 0, 1);
    procTask.addTask(1, 0, 2);
    otherAccountant.add(procStat.read());
    otherAccountant.add(procTask.read());

    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
    assertEquals(Accountant.Result.UNACCOUNTABLE, otherAccountant.account());

    accountant.add(otherAccountant);

    assertEquals(Accountant.Result.ACCOUNTED, accountant.account());
    double[] activity = new double[] {1.0 / 4.0, 2.0 / 4.0};
    for (ThreadActivity thread : accountant.process()) {
      assertEquals(activity[(int) thread.id], thread.activity, 0.0);
    }
  }
}
