package eflect.experiments;

import eflect.Eflect;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  private static final Eflect eflect = Eflect.getInstance();

  public DaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    eflect.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    eflect.start();
    System.out.println(System.getProperty("user.dir"));
    eflect.dump("eflect-data.pb");
  }

  @Override
  public boolean runAgain() {
    boolean doRun = super.runAgain();
    if (!doRun) {
      eflect.shutdown();
    }
    return doRun;
  }
}
