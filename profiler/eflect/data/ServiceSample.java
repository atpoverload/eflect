package eflect.data;

import java.time.Instant;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Snapshot of the application's jiffies broken down by service. */
public class ServiceSample implements Sample {
  // private final int[] jiffies;
  private final Instant timestamp;

  public ServiceSample() {
    // this.jiffies = readApplicationJiffies();
    this.timestamp = Instant.now();
  }

  public int[] getJiffies() {
    // return jiffies;
    return null;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
