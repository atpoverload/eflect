package eflect.sample;

import eflect.protos.sample.CpuStatReading;
import eflect.protos.sample.CpuStatSample;
import eflect.protos.sample.Sample;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/** Helper for reading jiffies from /proc/stat system. */
public final class ProcStatDataSources {
  // system information
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");

  // indicies for cpu stat because there are so many
  private enum CpuIndex {
    CPU(0),
    USER(1),
    NICE(2),
    SYSTEM(3),
    IDLE(4),
    IOWAIT(5),
    IRQ(6),
    SOFTIRQ(7),
    STEAL(8),
    GUEST(9),
    GUEST_NICE(10);

    private int index;

    private CpuIndex(int index) {
      this.index = index;
    }
  }

  /** Reads the cpu's stats and returns a sample from it. */
  public static Sample sampleCpuStats() {
    return Sample.newBuilder()
        .setCpu(readCpuStats().setTimestamp(Instant.now().toEpochMilli()))
        .build();
  }

  /** Reads the system's stat file and returns individual cpus. */
  private static String[] readCpus() {
    String[] stats = new String[CPU_COUNT];
    try (BufferedReader reader = Files.newBufferedReader(Path.of(SYSTEM_STAT_FILE))) {
      reader.readLine(); // first line is total summary; we need by cpu
      for (int i = 0; i < CPU_COUNT; i++) {
        stats[i] = reader.readLine();
      }
    } catch (Exception e) {
      System.out.println("unable to read " + SYSTEM_STAT_FILE);
    }

    return stats;
  }

  /** Turns task stat strings into a Sample. */
  private static CpuStatSample.Builder readCpuStats() {
    CpuStatSample.Builder sample = CpuStatSample.newBuilder();
    for (String statString : readCpus()) {
      String[] stat = statString.split(" ");
      if (stat.length != 11) {
        continue;
      }
      sample.addReading(
          CpuStatReading.newBuilder()
              .setCpu(Integer.parseInt(stat[CpuIndex.CPU.index].substring(3)))
              .setUser(Integer.parseInt(stat[CpuIndex.USER.index]))
              .setNice(Integer.parseInt(stat[CpuIndex.NICE.index]))
              .setSystem(Integer.parseInt(stat[CpuIndex.SYSTEM.index]))
              .setIdle(Integer.parseInt(stat[CpuIndex.IDLE.index]))
              .setIowait(Integer.parseInt(stat[CpuIndex.IOWAIT.index]))
              .setIrq(Integer.parseInt(stat[CpuIndex.IRQ.index]))
              .setSoftirq(Integer.parseInt(stat[CpuIndex.SOFTIRQ.index]))
              .setSteal(Integer.parseInt(stat[CpuIndex.STEAL.index]))
              .setGuest(Integer.parseInt(stat[CpuIndex.GUEST.index]))
              .setGuestNice(Integer.parseInt(stat[CpuIndex.GUEST_NICE.index])));
    }
    return sample;
  }

  private ProcStatDataSources() {}
}
