package eflect.utils;

import java.time.Instant;

/** Utilities providing algebra for {@link Instant}s. */
public final class TimeUtils {
  // boolean
  public static boolean atMost(Instant first, Instant second) {
    return first.compareTo(second) <= 0;
  }

  public static boolean atLeast(Instant first, Instant second) {
    return first.compareTo(second) >= 0;
  }

  public static boolean equal(Instant first, Instant second) {
    return first.compareTo(second) == 0;
  }

  public static boolean greaterThan(Instant first, Instant second) {
    return first.compareTo(second) > 0;
  }

  public static boolean lessThan(Instant first, Instant second) {
    return first.compareTo(second) < 0;
  }

  public static Instant max(Instant first, Instant second) {
    if (greaterThan(first, second)) {
      return first;
    } else {
      return second;
    }
  }

  // numeric
  public static Instant max(Instant first, Instant... others) {
    Instant minTimestamp = first;
    for (Instant other: others) {
      minTimestamp = max(minTimestamp, other);
    }
    return minTimestamp;
  }

  public static Instant min(Instant first, Instant second) {
    if (lessThan(first, second)) {
      return first;
    } else {
      return second;
    }
  }

  public static Instant min(Instant first, Instant... others) {
    Instant minTimestamp = first;
    for (Instant other: others) {
      minTimestamp = min(minTimestamp, other);
    }
    return minTimestamp;
  }


  // this is probably unsafe to have
  public static Instant maxBelowUpper(Instant first, Instant second) {
    if ((greaterThan(first, second) && !equal(first, Instant.MAX)) || equal(second, Instant.MAX)) {
      return first;
    } else {
      return second;
    }
  }

  public static Instant minAboveLower(Instant first, Instant second) {
    if ((lessThan(first, second) && !equal(first, Instant.MIN)) || equal(second, Instant.MIN)) {
      return first;
    } else {
      return second;
    }
  }
}
