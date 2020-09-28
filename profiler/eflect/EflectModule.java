package eflect;

import clerk.DataSource;
import clerk.Processor;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import eflect.data.CpuSample;
import eflect.data.RaplSample;
import eflect.data.Sample;
import eflect.data.TaskSample;
import java.util.Set;
import java.util.function.Supplier;

/** Module to provide the eflect implementation. */
@Module
public interface EflectModule {
  @Provides
  @DataSource
  @IntoSet
  static Supplier<?> provideTaskSource() {
    return TaskSample::new;
  }

  @Provides
  @DataSource
  @IntoSet
  static Supplier<?> provideCpuSource() {
    return CpuSample::new;
  }

  @Provides
  @DataSource
  @IntoSet
  static Supplier<?> provideRaplSource() {
    return RaplSample::new;
  }

  @Provides
  static Processor<?, Iterable<EnergyFootprint>> provideProcessor() {
    return new EflectProcessor();
  }
}
