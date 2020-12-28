package eflect.experiments;

import clerk.Clerk;
import clerk.util.ClerkUtil;
import eflect.DummyEflect;
import java.time.Duration;
import java.util.logging.Logger;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  private static final Logger logger = ClerkUtil.getLogger();

  private final Clerk<?> clerk;

  public DaCapo(CommandLineArgs args) {
    super(args);
    clerk = DummyEflect.newEflectClerk(Duration.ofMillis(41));
  }

  @Override
  public void start(String benchmark) {
    logger.info("starting eflect");
    clerk.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    clerk.stop();
    logger.info("stopped eflect");

    WriterUtils.writeCsv(
        "/mnt/c/Users/Timur/Documents/projects/eflect/eflect-footprints.csv",
        "id,name,start,end,energy,trace",
        (Iterable<?>) clerk.read());
  }
}
