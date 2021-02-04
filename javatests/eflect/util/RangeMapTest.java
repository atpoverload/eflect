package eflect.util;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

public class RangeMapTest {
  @Test
  public void contains_emptyMap() {
    RangeMap<Integer, ?> map = new RangeMap<>(emptyMap());
    assertFalse(map.contains(0));
  }

  @Test
  public void get_emptyMap() {
    RangeMap<Integer, ?> map = new RangeMap<>(emptyMap());
    assertEquals(null, map.get(0));
  }

  @Test
  public void contains_oneValue() {
    RangeMap<Integer, ?> map = new RangeMap<>(Map.of(0, true));
    assertFalse(map.contains(Integer.MIN_VALUE));
    assertFalse(map.contains(-1));
    assertTrue(map.contains(0));
    assertTrue(map.contains(Integer.MAX_VALUE));
  }

  @Test
  public void get_oneValue() {
    RangeMap<Integer, ?> map = new RangeMap<>(Map.of(0, true));
    assertEquals(null, map.get(Integer.MIN_VALUE));
    assertEquals(null, map.get(-1));
    assertEquals(true, map.get(0));
    assertEquals(true, map.get(Integer.MAX_VALUE));
  }

  @Test
  public void contains_twoValues() {
    int start = 0;
    int end = 10;
    RangeMap<Integer, ?> map = new RangeMap<>(Map.of(start, true, end, false));
    for (int i = start; i < end; i++) {
      assertTrue(map.contains(i));
    }
    assertTrue(map.contains(end));
  }

  @Test
  public void get_twoValues() {
    int start = 0;
    int end = 10;
    RangeMap<Integer, ?> map = new RangeMap<>(Map.of(start, true, end, false));
    for (int i = start; i < end; i++) {
      assertEquals(true, map.get(i));
    }
    assertEquals(false, map.get(end));
  }

  @Test
  public void get_threeValues() {
    int first = 0;
    int second = 5;
    int third = 10;
    RangeMap<Integer, ?> map = new RangeMap<>(Map.of(first, true, second, false, third, true));
    for (int i = first; i < second; i++) {
      assertEquals(true, map.get(i));
    }
    for (int i = second; i < third; i++) {
      assertEquals(false, map.get(i));
    }
    assertEquals(true, map.get(third));
  }
}
