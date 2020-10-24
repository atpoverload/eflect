package eflect.experiments;

import clerk.Clerk;
import clerk.util.ClerkLogger;
import eflect.Eflect;
import eflect.EnergyFootprint;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;
import java.util.logging.Logger;

public final class DaCapo extends Callback {
  private static final Logger logger = ClerkLogger.getLogger();

  private final String outputPath;
  private final Clerk<List<EnergyFootprint<?>>> eflect;

  private int iteration = 0;

  public DaCapo(CommandLineArgs args) {
    super(args);
    eflect = Eflect.newEflect();
    outputPath = System.getProperty("eflect.output", "data");
    new File(outputPath).mkdirs();
  }

  @Override
  public void start(String benchmark) {
    eflect.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    eflect.stop();

    List<EnergyFootprint<?>> footprints = eflect.dump();

    // print to console
    logger.info(footprints.toString());
    logger.info(String.format("consumed %.2fJ", Eflect.sum(footprints)));

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
