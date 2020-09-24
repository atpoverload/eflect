package eflect.data;

import static eflect.utils.OsUtils.readApplicationJiffies;

import java.time.Instant;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Snapshot of the application's jiffies. */
public class TaskSample implements Sample {
  private final int[] jiffies;
  private final Instant timestamp;

  public TaskSample() {
    this.jiffies = readApplicationJiffies();
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
