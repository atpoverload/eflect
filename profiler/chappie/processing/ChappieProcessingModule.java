package chappie.processing;

import clerk.Processor;
import dagger.Module;
import dagger.Provides;

/** Module to provide the eflect-chappie implementation. */
@Module
public interface ChappieProcessingModule {
  @Provides
  static Processor<?, ?> provideProcessor() {
    return new ChappieProcessor();
  }
}
