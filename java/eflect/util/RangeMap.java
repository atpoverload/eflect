package eflect.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A wrapper around a map that places keys in a range.
 *
 * <p>A key k's bucket is such that bucket.value <= k < (bucket + 1).value.
 */
public final class RangeMap<K extends Comparable, V> {
  private final Map<K, V> data;

  public RangeMap(Map<K, V> data) {
    this.data = data;
  }

  /** Checks if a key is in the key range of the data. */
  public boolean contains(K k) {
    return !data.isEmpty() && k != null && findKey(k) != null;
  }

  /** Gets the data in the bucket closest to the key. */
  public V get(K k) {
    if (!contains(k)) {
      return null;
    }
    return data.get(findKey(k));
  }

  /** Returns the underlying data. */
  public Collection<V> values() {
    return data.values();
  }

  public Collection<K> keys() {
    return data.keySet();
  }

  private K findKey(K k) {
    // TODO(timur): switch to binary search
    ArrayList<K> keys = new ArrayList<>(data.keySet());
    Collections.sort(keys);
    for (int i = 0; i < keys.size(); i++) {
      if (keys.get(i).compareTo(k) <= 0
          && (keys.size() > i + 1 && keys.get(i + 1).compareTo(k) > 0)) {
        return keys.get(i);
      }
    }
    return null;
  }
}
