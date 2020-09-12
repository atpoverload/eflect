package eflect;

import clerk.Processor;
import dagger.Module;
import dagger.Provides;
import eflect.data.CpuSample;
import eflect.data.RaplSample;
import eflect.data.Sample;
import eflect.data.TaskSample;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

/** Module to time how long the profiler has run. */
@Module
public interface EflectModule {
  @Provides
  static Iterable<Supplier<Sample>> provideSources() {
    return List.of(TaskSample::new, CpuSample::new, RaplSample::new);
  }

  @Provides
  static Processor<Sample, Iterable<EnergyFootprint>> provideProcessor() {
    return new EflectProcessor();
  }
}
