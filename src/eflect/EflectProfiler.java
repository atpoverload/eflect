package eflect;

import clerk.concurrent.PeriodicSchedulingModule;
import clerk.Profiler;
import dagger.Component;
import eflect.sampling.Sample;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

public class EflectProfiler {
  @Component(modules = {PeriodicSchedulingModule.class, EflectModule.class})
  interface ClerkFactory {
    Profiler<Sample, Integer> newClerk();
  }

  private static final ClerkFactory clerkFactory = DaggerEflectProfiler_ClerkFactory.builder().build();

  private static Profiler clerk;
  // private static Map<Instant, MemoryStats> profile = new TreeMap<>();

  // starts a profiler if there is not one
  public static void start() {
    if (clerk == null) {
      clerk = clerkFactory.newClerk();
      clerk.start();
    }
  }

  // stops the profiler if there is one
  public static void stop() {
    if (clerk != null) {
      // profile = (Map<Instant, MemoryStats>) clerk.stop();
      clerk = null;
    }
  }

  // restart the profiler so that we start fresh
  public static Integer dump() {
    if (clerk != null) {
      stop();
      start();
    }
    return 1; // profile;
  }
}
