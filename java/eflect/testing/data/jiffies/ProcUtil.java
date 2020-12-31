package eflect.testing.data.jiffies;

import java.util.Arrays;

/** A class that builds fake stats information. */
public final class ProcUtil {
  public static final String createDummyStats(int size) {
    String[] dummyStats = new String[size];
    Arrays.fill(dummyStats, "0");
    return String.join(" ", dummyStats);
  }

  private ProcUtil() {}
}
