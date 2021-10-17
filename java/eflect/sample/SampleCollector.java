package eflect.sample;

import clerk.storage.ListStorage;
import clerk.util.FixedPeriodClerk;
import eflect.protos.sample.DataSet;
import eflect.protos.sample.Sample;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that collects samples into a data set. */
public class SampleCollector extends FixedPeriodClerk<DataSet> {
  public SampleCollector(
      Collection<Supplier<? extends Sample>> sources,
      ScheduledExecutorService executor,
      Duration period) {
    super(
        sources,
        new ListStorage<Sample, DataSet>() {
          @Override
          public DataSet process() {
            DataSet.Builder dataSet = DataSet.newBuilder();
            getData()
                .forEach(
                    sample -> {
                      switch (sample.getDataCase()) {
                        case CPU:
                          dataSet.addCpu(sample.getCpu());
                          break;
                        case RAPL:
                          dataSet.addRapl(sample.getRapl());
                          break;
                        case TASK:
                          dataSet.addTask(sample.getTask());
                          break;
                      }
                    });
            return dataSet.build();
          }
        },
        executor,
        period);
  }
}
