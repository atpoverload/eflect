package eflect;

import static java.util.Collections.emptyList;

import clerk.Clerk;
import clerk.inject.ClerkModule;
import clerk.inject.PeriodicSamplingModule;
import clerk.util.ClerkLogger;
import eflect.data.EflectDataModule;
import eflect.processing.EflectProcessingModule;
import dagger.Component;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/** A profiler that estimates the energy consumed by the current application. */
public final class Eflect {
  private static final Logger logger = ClerkLogger.getLogger();

  @Component(modules = {EflectDataModule.class, EflectProcessingModule.class, ClerkModule.class, PeriodicSamplingModule.class})
  interface ClerkFactory {
    Clerk newClerk();
  }

  private static final ClerkFactory clerkFactory = DaggerEflect_ClerkFactory.builder().build();

  public static Clerk<List<EnergyFootprint<?>>> newEflect() {
    return clerkFactory.newClerk();
  }

  public static double sum(Iterable<EnergyFootprint<?>> profiles) {
    double energy = 0;
    for (EnergyFootprint profile: profiles) {
      energy += profile.getEnergy();
    }
    return energy;
  }

  private Eflect() { }

  public static void main(String[] args) throws Exception {
    Clerk<?> eflect = newEflect();
    eflect.start();
    Thread.sleep(10000);
    eflect.stop();
    System.out.println(eflect.dump());
  }
}
