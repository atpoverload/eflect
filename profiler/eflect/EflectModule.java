package eflect;

import clerk.DataSource;
import clerk.Processor;
import dagger.Module;
import dagger.Provides;
import eflect.data.CpuSample;
import eflect.data.RaplSample;
import eflect.data.Sample;
import eflect.data.TaskSample;
import java.util.Set;
import java.util.function.Supplier;

/** Module to provide the eflect implementation. */
@Module
interface EflectModule {
  @Provides
  @DataSource
  static Set<Supplier<?>> provideSources() {
    return Set.of(TaskSample::new, CpuSample::new, RaplSample::new);
  }

  @Provides
  static Processor<?, Iterable<EnergyFootprint>> provideProcessor() {
    return new EflectProcessor();
  }
}
