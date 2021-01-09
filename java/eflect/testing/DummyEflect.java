package eflect.testing;

import static eflect.util.ProcUtil.getTasks;
import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import clerk.FixedPeriodClerk;
import eflect.data.Accountant;
import eflect.data.AccountantMerger;
import eflect.data.EnergyAccountant;
import eflect.data.EnergyFootprint;
import eflect.data.EnergySample;
import eflect.data.StackTraceAligner;
import eflect.data.async.AsyncProfilerSample;
import eflect.data.jiffies.JiffiesAccountant;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/** A clerk that provides fake application energy. */
public final class DummyEflect extends FixedPeriodClerk<Collection<EnergyFootprint>> {
  private static Collection<Supplier<?>> getSources() {
    long start = System.currentTimeMillis();
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl =
        () ->
            new EnergySample(Instant.now(), new double[][] {{System.currentTimeMillis() - start}});
    // TODO(timur): it would be cool to create a fake stack trace generator
    Supplier<?> async =
        () -> {
          ArrayList<String> records = new ArrayList<>();
          for (String id : getTasks()) {
            records.add(
                String.join(
                    ",",
                    Long.toString(System.currentTimeMillis()),
                    id,
                    Integer.toString((int) (Math.random() * 1000000))));
          }
          return new AsyncProfilerSample(String.join(System.lineSeparator(), records));
        };
    return List.of(stat, task, rapl, async);
  }

  public DummyEflect(Duration period) {
    super(
        getSources(),
        new StackTraceAligner(
            new AccountantMerger<EnergyFootprint>() {
              @Override
              public Accountant<Collection<EnergyFootprint>> newAccountant() {
                return new EnergyAccountant(1, 0, new JiffiesAccountant(1, cpu -> 0));
              }
            }),
        period);
  }
}
