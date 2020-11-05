package eflect.data;

import clerk.inject.ClerkComponent;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.function.Supplier;

/** Module to provide the eflect data sources. */
@Module
public interface EflectDataModule {
  @Provides
  @ClerkComponent
  @IntoMap
  @StringKey("task_jiffies_source")
  static Supplier<?> provideTaskData() {
    return TaskSample::readTaskStats;
  }

  @Provides
  @ClerkComponent
  @IntoMap
  @StringKey("machine_jiffies_source")
  static Supplier<?> provideMachineData() {
    return MachineSample::readMachineStats;
  }

  @Provides
  @ClerkComponent
  @IntoMap
  @StringKey("rapl_source")
  static Supplier<?> provideRaplData() {
    return RaplSample::readEnergy;
  }
}
