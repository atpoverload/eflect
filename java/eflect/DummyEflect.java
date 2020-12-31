package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import clerk.Clerk;
import clerk.FixedPeriodClerk;
import eflect.data.Accountant;
import eflect.data.AccountantMerger;
import eflect.data.EnergyAccountant;
import eflect.data.EnergyFootprint;
import eflect.data.jiffies.JiffiesAccountant;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import eflect.testing.data.DummyEnergySample;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A profiler that estimates the energy consumed by the current application using jiffies and rapl.
 */
public final class DummyEflect {
  public static Clerk<Collection<EnergyFootprint>> newEflectClerk(Duration period) {
    final AtomicInteger counter = new AtomicInteger();
    Supplier<?> procStat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> procTask = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new DummyEnergySample(Instant.now(), counter.getAndIncrement());
    return new FixedPeriodClerk(
        List.of(procStat, procTask, rapl),
        new EnergyAccountantMerger(
            () -> new EnergyAccountant(1, 0, new JiffiesAccountant(1, cpu -> 0))),
        period);
  }

  private DummyEflect() {}

  private static final class EnergyAccountantMerger extends AccountantMerger<EnergyFootprint> {
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
