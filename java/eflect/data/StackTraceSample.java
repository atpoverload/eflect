package eflect.data;

/** Sample of a thread's current executing trace. */
public interface StackTraceSample extends Sample {
  /** Returns the executing thread's id. */
  long getId();

  /** Returns the executing stack trace. */
  String getStackTrace();
}
