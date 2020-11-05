package eflect.processing;

import clerk.Processor;
import dagger.Module;
import dagger.Provides;
import eflect.EnergyFootprint;
import java.util.List;

/** Module to provide the eflect implementation. */
@Module
public interface EflectProcessingModule {
  @Provides
  static Processor<?, List<EnergyFootprint<?>>> provideProcessor() {
    return new EflectProcessor();
  }
}
