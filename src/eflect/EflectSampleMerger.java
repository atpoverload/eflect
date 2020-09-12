package eflect;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import clerk.Processor;
import eflect.data.CpuSample;
import eflect.data.RaplSample;
import eflect.data.Sample;
import eflect.data.TaskSample;
import eflect.utils.TimeUtils;
import java.time.Instant;

final class EflectSampleMerger implements Processor<Sample, EnergyFootprint> {
  private Instant start = Instant.MAX;
  private Instant end = Instant.MIN;
  private final int[] app = new int[SOCKETS];
  private final int[] cpu = new int[SOCKETS];
  private final double[] energy = new double[SOCKETS];

  @Override
  public void add(Sample s) {
    // bad; think about another separation mechanism
    if (s instanceof TaskSample) {
      for (int i = 0; i < SOCKETS; i++) {
        this.app[i] = ((TaskSample) s).getJiffies()[i];
      }
    } else if (s instanceof CpuSample) {
      for (int i = 0; i < SOCKETS; i++) {
        this.cpu[i] = ((CpuSample) s).getJiffies()[i];
      }
    } else if (s instanceof RaplSample) {
      for (int i = 0; i < SOCKETS; i++) {
        this.energy[i] = ((RaplSample) s).getEnergy()[i];
      }
    } else {
      return; // break out so the timestamp isn't touched
    }

    Instant timestamp = s.getTimestamp();
    start = TimeUtils.min(timestamp, start);
    end = TimeUtils.max(timestamp, end);
  }

  @Override
  public EnergyFootprint process() {
    double[] energy = new double[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      // compute the attribution factor
      double factor = 0;
      if (this.cpu[socket] > 0) {
        factor = (double)(Math.min(this.app[socket], this.cpu[socket])) / this.cpu[socket];
      }
      energy[socket] = factor * this.energy[socket];
    }
    return new EnergyFootprint(start, end, energy);
  }

  EflectSampleMerger merge(EflectSampleMerger other) {
    EflectSampleMerger merged = new EflectSampleMerger();

    for (int socket = 0; socket < SOCKETS; socket++) {
      merged.app[socket] = Math.max(this.app[socket], this.app[socket]);
      merged.cpu[socket] = Math.max(this.cpu[socket], this.cpu[socket]);
      merged.energy[socket] = Math.max(this.energy[socket], this.energy[socket]);
    }

    return merged;
  }

  boolean valid() {
    if (!attributable()) {
      return false;
    }

    int energyConsumed = 0;
    for (int socket = 0; socket < SOCKETS; socket++) {
      energyConsumed += energy.getEnergy(socket);
    }

    if (energyConsumed == 0) {
      return false;
    }

    for (int socket = 0; socket < SOCKETS; socket++) {
      if (tasks.getJiffies(socket) > cpu.getJiffies(socket)) {
        return false;
      }
    }

    return true;
  }

  Instant getTimestamp() {
    return end;
  }

  // this is supposed to determine if we produce a footprint; should think about how to customize this
  private boolean valid() {
    if (TimeUtils.equal(start, Instant.MAX) || TimeUtils.equal(end, Instant.MIN)) {
      return false;
    }

    for (int socket = 0; socket < SOCKETS; socket++) {
      if (this.energy[socket] == 0) {
        return false;
      }
    }

    for (int socket = 0; socket < SOCKETS; socket++) {
      if (cpu.getJiffies(socket) < 0) {
        return false;
      }
    }

    return true;
  }
}
