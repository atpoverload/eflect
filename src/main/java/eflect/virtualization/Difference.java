package eflect.virtualization;

import com.google.protobuf.Timestamp;
import java.util.ArrayList;
import java.util.List;

final class Difference<T> {
  public static <T> Difference<T> of(Timestamp start, Timestamp end, List<T> data) {
    return new Difference<>(start, end, data);
  }

  public final Timestamp start;
  public final Timestamp end;
  private final ArrayList<T> data;

  private Difference(Timestamp start, Timestamp end, List<T> data) {
    this.start = start;
    this.end = end;
    this.data = new ArrayList<>(data);
  }

  public List<T> getData() {
    return (List<T>) data.clone();
  }

  @Override
  public String toString() {
    return String.join(",", start.toString(), end.toString()) + "\n" + data;
  }
}
