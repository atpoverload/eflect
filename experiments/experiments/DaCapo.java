package eflect.experiments;

import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  public DaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    EflectMonitor.getInstance().start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    EflectMonitor.getInstance().stop();
    EflectMonitor.getInstance().dump();
  }

  @Override
  public boolean runAgain() {
    boolean doRun = super.runAgain();
    if (!doRun) {
      EflectMonitor.getInstance().shutdown();
    }
    return doRun;
  }
}
