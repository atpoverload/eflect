package eflect.data;

/** Sample of a thread's current executing trace. */
public interface StackTraceSample extends Sample {
  long getId();

  String getStackTrace();
}
