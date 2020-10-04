package eflect;

import static eflect.utils.OsUtils.setProcessId;
import static java.util.Collections.emptyList;

import clerk.Profiler;
import clerk.Clerk;
import clerk.concurrent.PeriodicSamplingModule;
import dagger.Component;
import eflect.data.Sample;
import java.io.File;
import java.util.List;

/** A profiler that estimates the energy consumed by the current application. */
public final class Eflect implements Profiler<List<EnergyFootprint>> {
  @Component(modules = {EflectModule.class, PeriodicSamplingModule.class})
  interface ClerkFactory {
    Clerk<List<EnergyFootprint>> newClerk();
  }

  private static final ClerkFactory clerkFactory = DaggerEflect_ClerkFactory.builder().build();

  private Clerk<List<EnergyFootprint>> clerk;

  public Eflect() { }

  // starts a profiler if there is not one
  public void start() {
    if (clerk == null) {
      clerk = clerkFactory.newClerk();
      clerk.start();
    }
  }

  // stops the profiler if there is one
  public List<EnergyFootprint> stop() {
    List<EnergyFootprint> profiles = emptyList();
    if (clerk != null) {
      profiles = (List<EnergyFootprint>) clerk.stop();
      clerk = null;
    }
    return profiles;
  }

  public static double sum(Iterable<EnergyFootprint> profiles) {
    double energy = 0;
    for (EnergyFootprint profile: profiles) {
      energy += profile.getEnergy();
    }
    return energy;
  }

  public static void main(String[] args) throws Exception {
    String pid = args[0];
    File procPid = new File("/proc", args[0]);
    setProcessId(Integer.parseInt(pid));

    Eflect eflect = new Eflect();
    eflect.start();
    while (procPid.exists()) { }
    System.out.println(String.join(" ",
      "pid",
      pid,
      "consumed",
      String.format("%.2f", sum(eflect.stop())) + "J"));
  }
}
