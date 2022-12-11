package eflect;

import static org.junit.Assert.assertTrue;

import eflect.protos.sample.DataSet;
import org.junit.After;
import org.junit.Test;

public class EflectTest {
  @After
  public void tearDown() {
    Eflect.getInstance().shutdown();
  }

  @Test
  public void linux_smokeTest() throws Exception {
    Eflect.getInstance().start();
    Thread.sleep(1000);
    Eflect.getInstance().stop();
    DataSet data = Eflect.getInstance().read();

    assertTrue("the cpu jiffies were not read", data.getCpuCount() > 0);
    assertTrue("the process jiffies were not read", data.getProcessCount() > 0);
    assertTrue("rapl was not read", data.getRaplCount() > 0);
  }
}
