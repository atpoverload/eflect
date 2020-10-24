package chappie.data;

import clerk.inject.ClerkComponent;
import eflect.util.TimeUtil;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/** Module to provide the chappie data sources. */
@Module
public interface ChappieDataModule {
  static final int DEFAULT_RATE_MS = 10;
  // TODO(timurbey): this should probably come from a module
  static final Duration chappieRate =
      Duration.ofMillis(
          Long.parseLong(
              System.getProperty("chappie.rate.sampling", Integer.toString(DEFAULT_RATE_MS))));

  @Provides
  @ClerkComponent
  @IntoSet
  static Supplier<?> provideAsyncSource() {
    return new Supplier<AsyncProfilerSample>() {
      private Instant last = Instant.EPOCH;

      @Override
      public AsyncProfilerSample get() {
        Instant current = Instant.now();
        if (TimeUtil.atLeast(current, last.plus(chappieRate))) {
          AsyncProfilerSample sample = new AsyncProfilerSample();
          last = current;
          return sample;
        }
        return null;
      }
    };
  }
}
