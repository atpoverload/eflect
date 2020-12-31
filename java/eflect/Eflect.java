package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;
import static jrapl.Rapl.SOCKET_COUNT;
import static jrapl.Rapl.WRAP_AROUND_ENERGY;
import static jrapl.Rapl.getEnergyStats;

import clerk.Clerk;
import clerk.FixedPeriodClerk;
import eflect.data.EnergyFootprint;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import eflect.data.rapl.RaplSample;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * A profiler that estimates the energy consumed by the current application using jiffies and rapl.
 */
public final class Eflect {
  public static Clerk<Collection<EnergyFootprint>> newEflectClerk(Duration period) {
    Supplier<?> procStat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> procTask = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new RaplSample(Instant.now(), getEnergyStats());
    return new FixedPeriodClerk(
        List.of(procStat, procTask, rapl),
        new EnergyAccountantMerger(
            () ->
                new EnergyAccountant(
                    SOCKET_COUNT,
                    WRAP_AROUND_ENERGY,
                    new JiffiesAccountant(SOCKET_COUNT, cpu -> cpu / (CPU_COUNT / SOCKET_COUNT)))),
        period);
  }

  private Eflect() {}

  public final class EnergyAccountantMerger extends AccountantMerger<EnergyFootprint> {
    private final Supplier<Accountant<Collection<EnergyFootprint>>> accountantFactory;

    public EnergyAccountantMerger(
        Supplier<Accountant<Collection<EnergyFootprint>>> accountantFactory) {
      this.accountantFactory = accountantFactory;
    }

    @Override
    public Accountant<Collection<EnergyFootprint>> newAccountant() {
      return accountantFactory.get();
    }
  }

  public static void main(String[] args) throws Exception {
    Clerk<?> eflect = newEflectClerk(Duration.ofMillis(16));
    eflect.start();
    Thread.sleep(10000);
    eflect.stop();
    System.out.println(eflect.read());
  }
}
