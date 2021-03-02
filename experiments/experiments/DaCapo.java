package eflect.experiments;

import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private String benchmark;
  private int iteration = 0;

  public DaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    this.benchmark = benchmark;
    EflectCalmnessMonitor.getInstance().start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    EflectCalmnessMonitor.getInstance().stop();
    EflectCalmnessMonitor.getInstance().dump(Integer.toString(iteration++));
  }

  @Override
  public boolean runAgain() {
    boolean doRun = super.runAgain();
    if (!doRun) {
      EflectCalmnessMonitor.getInstance().shutdown();
    }
    return doRun;
  }
}
