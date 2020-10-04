package eflect;

import clerk.DataSource;
import clerk.Processor;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import eflect.data.ApplicationSample;
import eflect.data.MachineSample;
import eflect.data.RaplSample;
import java.util.List;
import java.util.function.Supplier;

/** Module to provide the eflect implementation. */
@Module
public interface EflectModule {
  @Provides
  @DataSource
  @IntoSet
  static Supplier<?> provideApplicationSource() {
    return ApplicationSample::new;
  }

  @Provides
  @DataSource
  @IntoSet
  static Supplier<?> provideMachineSource() {
    return MachineSample::new;
  }

  @Provides
  @DataSource
  @IntoSet
  static Supplier<?> provideRaplSource() {
    return RaplSample::new;
  }

  @Provides
  static Processor<?, List<EnergyFootprint>> provideProcessor() {
    return new EflectProcessor();
  }
}
