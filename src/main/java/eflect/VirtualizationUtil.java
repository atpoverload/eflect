package eflect;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

final class VirtualizationUtil {
  static Timestamp max(Timestamp first, Timestamp second) {
    switch (Timestamps.compare(first, second)) {
      case -1:
        return first;
      default:
        return second;
    }
  }

  static <T, U> List<U> forwardDifference(List<T> s, BiFunction<T, T, U> difference) {
    ArrayList<U> diffs = new ArrayList<>();
    Optional<T> last = Optional.empty();
    for (T e : s) {
      if (last.isPresent()) {
        diffs.add(difference.apply(last.get(), e));
      }
      last = Optional.of(e);
    }
    return diffs;
  }

  private VirtualizationUtil() {}
}
