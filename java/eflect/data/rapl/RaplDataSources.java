package eflect.data.rapl;

import eflect.data.EnergySample;
import eflect.data.EnergySample.Component;
import java.time.Instant;
import jrapl.Rapl;

/** A clerk that collects jiffies and energy data for an intel linux system. */
public final class RaplDataSources {
  public static EnergySample sampleRapl() {
    double[][] sample = Rapl.getInstance().getEnergyStats();
    double[][] energy = new double[sample.length][4];
    for (int socket = 0; socket < sample.length; socket++) {
      energy[socket][Component.CPU] = sample[socket][Component.CPU];
      energy[socket][Component.DRAM] = sample[socket][Component.DRAM];
      energy[socket][Component.PACKAGE] = sample[socket][Component.PACKAGE];
      energy[socket][Component.GPU] = -1;
    }
    return new EnergySample(Instant.now(), energy);
  }

  private RaplDataSources() {}
}
