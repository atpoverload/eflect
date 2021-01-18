package eflect.data;

import clerk.Processor;

/** A processor that can report if {@link process()} will provide a meaningful result. */
// TODO(timur): these accountants don't explicitly remove data
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

  /** Discards the data at the beginning of the data interval. */
  void discardStart();

  /** Discards the data at the end of the data interval. */
  void discardEnd();
}
