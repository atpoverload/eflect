package eflect.data;

import clerk.inject.ClerkComponent;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.function.Supplier;

/** Module to provide the eflect data sources. */
@Module
public interface EflectDataModule {
  @Provides
  @ClerkComponent
  @IntoSet
  static Supplier<?> provideTaskData() {
    return TaskSample::new;
  }

  @Provides
  @ClerkComponent
  @IntoSet
  static Supplier<?> provideMachineData() {
    return MachineSample::new;
  }

  @Provides
  @ClerkComponent
  @IntoSet
  static Supplier<?> provideRaplData() {
    return RaplSample::new;
  }
}
