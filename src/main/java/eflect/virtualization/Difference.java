package eflect.virtualization;

import com.google.protobuf.Timestamp;
import java.util.ArrayList;
import java.util.List;

final class Difference<T> {
  final Timestamp start;
  final Timestamp end;
  final ArrayList<T> data;

  Difference(Timestamp start, Timestamp end, List<T> data) {
    this.start = start;
    this.end = end;
    this.data = new ArrayList<>(data);
  }

  @Override
  public String toString() {
    return String.join(",", start.toString(), end.toString()) + "\n" + data;
  }
}
