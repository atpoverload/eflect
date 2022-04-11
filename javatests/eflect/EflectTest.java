package eflect;

import static org.junit.Assert.assertTrue;

import eflect.protos.sample.DataSet;
import org.junit.After;
import org.junit.Test;

public class EflectTest {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final long PID = ProcessHandle.current().pid();

  @After
  public void tearDown() {
    Eflect.getInstance().shutdown();
  }

  @Test
  public void smokeTest() throws Exception {
    Eflect.getInstance().start();
    Thread.sleep(100);
    Eflect.getInstance().stop();
    DataSet data = Eflect.getInstance().read();

    assertTrue(data.getCpuCount() > 0);
    assertTrue(data.getProcessCount() > 0);
    assertTrue(data.getRaplCount() > 0);
  }
}
