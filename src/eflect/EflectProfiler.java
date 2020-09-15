package eflect;

import static java.util.Collections.emptyList;

import clerk.concurrent.PeriodicSamplingModule;
import clerk.Profiler;
import dagger.Component;
import eflect.data.Sample;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

/** A profiler that estimates the energy consumed by the current application. */
public class EflectProfiler {
  @Component(modules = {EflectModule.class, PeriodicSamplingModule.class})
  interface ClerkFactory {
    Profiler<Sample, Iterable<EnergyFootprint>> newClerk();
  }

  private static final ClerkFactory clerkFactory = DaggerEflectProfiler_ClerkFactory.builder().build();

  private static Profiler clerk;

  // starts a profiler if there is not one
  public static void start() {
    if (clerk == null) {
      clerk = clerkFactory.newClerk();
      clerk.start();
    }
  }

  // stops the profiler if there is one
  public static Iterable<EnergyFootprint> stop() {
    Iterable<EnergyFootprint> profile = emptyList();
    if (clerk != null) {
      profile = (Iterable<EnergyFootprint>) clerk.stop();
      clerk = null;
    }
    return profile;
  }
}
