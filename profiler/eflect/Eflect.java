package eflect;

import clerk.inject.Clerk;
import clerk.inject.ClerkExecutorModule;
import clerk.util.ClerkUtil;
import dagger.Component;
import eflect.data.EflectDataModule;
import eflect.processing.EflectProcessingModule;
import java.util.List;
import java.util.logging.Logger;

/** A profiler that estimates the energy consumed by the current application. */
public final class Eflect {
  private static final Logger logger = ClerkUtil.getLogger();

  @Component(
    modules = {EflectDataModule.class, EflectProcessingModule.class, ClerkExecutorModule.class}
  )
  interface ClerkFactory {
    Clerk<List<EnergyFootprint<?>>> newClerk();
  }

  private static final ClerkFactory clerkFactory = DaggerEflect_ClerkFactory.builder().build();

  public static Clerk<List<EnergyFootprint<?>>> newEflect() {
    return clerkFactory.newClerk();
  }

  public static double sum(Iterable<EnergyFootprint<?>> profiles) {
    double energy = 0;
    for (EnergyFootprint profile : profiles) {
      energy += profile.getEnergy();
    }
    return energy;
  }

  private Eflect() {}

  public static void main(String[] args) throws Exception {
    Clerk<?> eflect = newEflect();
    eflect.start();
    Thread.sleep(10000);
    eflect.stop();
    System.out.println(eflect.read());
  }
}
