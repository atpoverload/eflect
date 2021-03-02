package eflect;

import clerk.FixedPeriodClerk;
import eflect.data.Accountant;
import eflect.data.AccountantMerger;
import eflect.data.EnergyAccountant;
import eflect.data.EnergyFootprint;
import eflect.data.StackTraceAligner;
import eflect.data.ThreadActivity;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that uses the eflect algorithm as a {@link Processor}. */
public abstract class Eflect extends FixedPeriodClerk<Collection<EnergyFootprint>> {
  protected Eflect(
      Collection<Supplier<?>> sources,
      int domainCount,
      int componentCount,
      double wrapAroundEnergy,
      Supplier<Accountant<Collection<ThreadActivity>>> activityAccountantFactory,
      int mergeAttempts,
      ScheduledExecutorService executor,
      Duration period) {
    super(
        sources,
        new StackTraceAligner(
            new AccountantMerger<EnergyFootprint>(
                () ->
                    new EnergyAccountant(
                        domainCount,
                        componentCount,
                        wrapAroundEnergy,
                        activityAccountantFactory.get()),
                mergeAttempts)),
        executor,
        period);
  }
}
