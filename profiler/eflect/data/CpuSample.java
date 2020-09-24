package eflect.data;

import static eflect.utils.OsUtils.readMachineJiffies;

import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Snapshot of the machine's jiffies. */
public final class CpuSample implements Sample {
  private final int[] jiffies;
  private final Instant timestamp;

  public CpuSample() {
    this.jiffies = readMachineJiffies();
    this.timestamp = Instant.now();
  }

  public int[] getJiffies() {
    return jiffies;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
