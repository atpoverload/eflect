package eflect.data.jiffies;

import static org.junit.Assert.assertEquals;

import eflect.data.Accountant;
import eflect.testing.data.jiffies.FakeProcStat;
import eflect.testing.data.jiffies.FakeProcTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for operations when there isn't enough data to accountant. */
public class JiffiesAccountantTest_NotEnoughData {
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
  public void account_noData() {
    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_noTasksData() {
    accountant.add(procStat.read());
    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_noStatData() {
    accountant.add(procTask.read());
    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_notEnoughData() {
    accountant.add(procStat.read());
    accountant.add(procTask.read());
    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_noCpuJiffies() {
    procTask.addTask(0, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procTask.reset();
    procTask.addTask(0, 0, 1);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }

  @Test
  public void account_noTaskJiffies() {
    procTask.addTask(0, 0, 0);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    procStat.setJiffies(0, 1);
    accountant.add(procStat.read());
    accountant.add(procTask.read());

    assertEquals(Accountant.Result.UNACCOUNTABLE, accountant.account());
  }
}
