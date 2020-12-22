package eflect.experiments;

import clerk.Clerk;
import clerk.util.ClerkUtil;
import eflect.Eflect;
import eflect.data.EnergyFootprint;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;
import java.util.Collection;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  private static final Logger logger = ClerkUtil.getLogger();

  private final String outputPath;
  private final Clerk<?> clerk;

  private int iteration = 0;

  public DaCapo(CommandLineArgs args) {
    super(args);
    clerk = Eflect.newEflectClerk(Duration.ofMillis(16));
    outputPath = System.getProperty("eflect.output", "data");
    new File(outputPath).mkdirs();
  }

  @Override
  public void start(String benchmark) {
    clerk.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    clerk.stop();

    // Collection<?> footprints = clerk.read();

    // print to console
    logger.info(clerk.read().toString());
    // logger.info(String.format("consumed %.2fJ", Eflect.sum(footprints)));

    // write as a csv
    // try (PrintWriter writer = new PrintWriter(new FileWriter(new File(outputPath, iteration + ".csv")))) {
    //   writer.println("start,end,energy");
    //   for (EnergyFootprint<?> footprint: eflect.dump()) {
    //     writer.println(footprint.getStart() + "," + footprint.getEnd() + "," + footprint.getEnergy());
    //   }
    // } catch (IOException e) {
    //   System.out.println("couldn't write eflect log");
    //   e.printStackTrace();
    // }
    // iteration++;
  }
}
