package eflect.data;

import clerk.storage.ClassMappedListStorage;
import clerk.util.FixedPeriodClerk;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that collects samples into a class-mapped storage. */
public class SampleCollector extends FixedPeriodClerk<Map<Class<?>, List<Sample>>> {
  public SampleCollector(
      Collection<Supplier<? extends Sample>> sources,
      ScheduledExecutorService executor,
      Duration period) {
    super(
        sources,
        new ClassMappedListStorage<Sample, Map<Class<?>, List<Sample>>>() {
          @Override
          public Map<Class<?>, List<Sample>> process() {
            return getData();
          }
        },
        executor,
        period);
  }
}
