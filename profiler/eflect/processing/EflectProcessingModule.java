package eflect.processing;

import clerk.Processor;
import dagger.Module;
import dagger.Provides;

/** Module to provide the eflect implementation. */
@Module
public interface EflectProcessingModule {
  @Provides
  static Processor<?, ?> provideProcessor() {
    return new EflectProcessor();
  }
}
