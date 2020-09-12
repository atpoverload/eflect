package eflect;

import static eflect.utils.OsUtils.getProcessId;
import static jrapl.util.EnergyCheckUtils.SOCKETS;
import static jrapl.util.EnergyCheckUtils.ENERGY_WRAP_AROUND;

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

  private final int[] startApp = new int[SOCKETS];
  private final int[] startCpu = new int[SOCKETS];
  private final double[] startEnergy = new double[SOCKETS];

  private final int[] endApp = new int[SOCKETS];
  private final int[] endCpu = new int[SOCKETS];
  private final double[] endEnergy = new double[SOCKETS];

  @Override
  public void add(Sample s) {
    // bad; think about another separation mechanism
    if (s instanceof TaskSample) {
      for (int i = 0; i < SOCKETS; i++) {
        this.startApp[i] = ((TaskSample) s).getJiffies()[i];
        this.endApp[i] = ((TaskSample) s).getJiffies()[i];
      }
    } else if (s instanceof CpuSample) {
      for (int i = 0; i < SOCKETS; i++) {
        this.startCpu[i] = ((CpuSample) s).getJiffies()[i];
        this.endCpu[i] = ((CpuSample) s).getJiffies()[i];
      }
    } else if (s instanceof RaplSample) {
      for (int i = 0; i < SOCKETS; i++) {
        this.startEnergy[i] = ((RaplSample) s).getEnergy()[i];
        this.endEnergy[i] = ((RaplSample) s).getEnergy()[i];
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
    double[] appEnergy = new double[SOCKETS];
    for (int socket = 0; socket < SOCKETS; socket++) {
      double energy = endEnergy[socket] - startEnergy[socket];
      if (energy < 0) {
        energy += ENERGY_WRAP_AROUND;
      }

      int app = endApp[socket] - startApp[socket];
      int cpu = endCpu[socket] - startCpu[socket];

      // compute the attribution factor
      double factor = 0;
      if (cpu > 0) {
        factor = (double)(Math.min(app, cpu)) / cpu;
      }
      appEnergy[socket] = factor * energy;
    }
    return new EnergyFootprint(start, end, appEnergy);
  }

  EflectSampleMerger merge(EflectSampleMerger other) {
    EflectSampleMerger merged = new EflectSampleMerger();

    for (int socket = 0; socket < SOCKETS; socket++) {
      merged.startApp[socket] = this.startApp[socket] == 0
        ? other.startApp[socket]
        : other.startApp[socket] == 0 ? this.startApp[socket]
        : Math.min(this.startApp[socket], other.startApp[socket]);
      merged.endApp[socket] = Math.max(this.endApp[socket], other.endApp[socket]);

      merged.startCpu[socket] = this.startCpu[socket] == 0
        ? other.startCpu[socket]
        : other.startCpu[socket] == 0 ? this.startCpu[socket]
        : Math.min(this.startCpu[socket], other.startCpu[socket]);
      merged.endCpu[socket] = Math.max(this.endCpu[socket], other.endCpu[socket]);

      merged.startEnergy[socket] = this.startEnergy[socket] == 0
        ? other.startEnergy[socket]
        : other.startEnergy[socket] == 0 ? this.startEnergy[socket]
        : Math.min(this.startEnergy[socket], other.startEnergy[socket]);
      merged.endEnergy[socket] = Math.max(this.endEnergy[socket], other.endEnergy[socket]);
    }

    merged.start = TimeUtils.min(this.start, other.start);
    merged.end = TimeUtils.max(this.end, other.end);

    return merged;
  }

  boolean valid() {
    if (TimeUtils.equal(start, Instant.MAX) || TimeUtils.equal(end, Instant.MIN)) {
      return false;
    }

    for (int socket = 0; socket < SOCKETS; socket++) {
      double energy = endEnergy[socket] - startEnergy[socket];
      if (energy < 0) {
        energy += ENERGY_WRAP_AROUND;
      }
      int app = endApp[socket] - startApp[socket];
      int cpu = endCpu[socket] - startCpu[socket];
      if (energy == 0 || cpu == 0 || app == 0 || app > cpu) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return String.join(",",
      start.toString(),
      end.toString(),
      Integer.toString(startApp[0]), Integer.toString(endApp[0]),
      Integer.toString(startCpu[0]), Integer.toString(endCpu[0]),
      Double.toString(startEnergy[0]), Double.toString(endEnergy[0]));
  }
}
