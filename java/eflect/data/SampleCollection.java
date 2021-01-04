package eflect.data;

import java.util.Collection;

/** Interface for a collection of {@link Sample}s. */
public interface SampleCollection<O extends Sample> extends Sample {
  Collection<O> getSamples();
}
