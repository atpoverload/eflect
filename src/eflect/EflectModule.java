package eflect;

import clerk.DataSource;
import clerk.Processor;
import dagger.Module;
import dagger.Provides;
import eflect.data.CpuSample;
import eflect.data.RaplSample;
import eflect.data.Sample;
import eflect.data.TaskSample;
import java.util.List;
import java.util.function.Supplier;

/** Module to provide the eflect implementation. */
@Module
public interface EflectModule {
  @Provides
  @DataSource
  static Iterable<Supplier<Sample>> provideSources() {
    return List.of(TaskSample::new, CpuSample::new, RaplSample::new);
  }

  @Provides
  static Processor<Sample, Iterable<EnergyFootprint>> provideProcessor() {
    return new EflectProcessor();
  }
}
