package eflect.data;

import clerk.Processor;

/** A processor that can report if {@link process()} will provide a meaningful result. */
public interface Accountant<O> extends Processor<Sample, O> {
  /** Enum that indicates how the user should interpret the result. */
  public enum Result {
    UNACCOUNTABLE,
    UNDERACCOUNTED,
    OVERACCOUNTED,
    ACCOUNTED,
  }

  /** Returns true if the output of process() will be meaningful. */
  Result account();

  /** Adds the data of this accountant with another. */
  <T extends Accountant<O>> void add(T other);
}
