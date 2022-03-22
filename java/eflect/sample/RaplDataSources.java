package eflect.sample;

import eflect.protos.sample.RaplReading;
import eflect.protos.sample.RaplSample;
import eflect.protos.sample.Sample;
import java.time.Instant;
import jrapl.Rapl;

/** Static helper that packs jRapl readings into a proto. */
public final class RaplDataSources {
  public static Sample sampleRapl() {
    RaplSample.Builder sample = RaplSample.newBuilder().setTimestamp(Instant.now().toEpochMilli());
    double[][] stats = Rapl.getInstance().getEnergyStats();
    for (int socket = 0; socket < stats.length; socket++) {
      sample.addReading(
          RaplReading.newBuilder()
              .setSocket(socket)
              .setDram((long) (1000000 * stats[socket][0]))
              .setPackage((long) (1000000 * stats[socket][2])));
    }
    return Sample.newBuilder().setRapl(sample).build();
  }

  private RaplDataSources() {}
}
