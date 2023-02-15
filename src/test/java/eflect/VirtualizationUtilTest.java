package eflect;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.Test;

public class VirtualizationUtilTest {
  private static final int N = 7855; // some random 4 digit number i generated
  private static final List<Integer> X = IntStream.range(0, N).boxed().collect(toList());

  private static final BiFunction<Integer, Integer, Integer> INTEGER_DIFF = (i1, i2) -> i2 - i1;
  private static final BiFunction<Double, Double, Double> DOUBLE_DIFF =
      (i1, i2) -> Math.round(N * N * (i2 - i1)) / ((double) N * N);

  private static final Vector ZERO = new Vector(0, 0);
  private static final Vector RIGHT = new Vector(1, 0);
  private static final Vector UP = new Vector(0, 1);

  private static final BiFunction<Vector, Vector, Vector> VECTOR_DIFF =
      (i1, i2) -> new Vector(i2.x - i1.x, i2.y - i1.y);
  private static final BiFunction<List<Vector>, List<Vector>, List<Vector>> FIELD_DIFF =
      (i1, i2) ->
          IntStream.range(0, i1.size())
              .mapToObj(i -> VECTOR_DIFF.apply(i1.get(0), i2.get(0)))
              .collect(toList());

  @Test
  public void forwardDifference_numbers() {
    assertEquals(List.of(1), VirtualizationUtil.forwardDifference(List.of(0, 1), INTEGER_DIFF));

    // derivate of x^2
    assertEquals(
        X.stream().limit(N - 1).map(i -> 2 * i + 1).collect(toList()),
        VirtualizationUtil.forwardDifference(
            X.stream().map(i -> i * i).collect(toList()), INTEGER_DIFF));

    List<Double> x = X.stream().map(i -> (double) i / (double) N).collect(toList());
    assertEquals(
        X.stream()
            .limit(N - 1)
            .map(i -> ((double) 2 * i + 1) / ((double) (N * N)))
            .collect(toList()),
        VirtualizationUtil.forwardDifference(
            x.stream().map(i -> i * i).collect(toList()), DOUBLE_DIFF));
  }

  @Test
  public void forwardDifference_vectors() {
    assertEquals(
        List.of(RIGHT), VirtualizationUtil.forwardDifference(List.of(ZERO, RIGHT), VECTOR_DIFF));

    assertEquals(List.of(UP), VirtualizationUtil.forwardDifference(List.of(ZERO, UP), VECTOR_DIFF));

    assertEquals(
        List.of(UP, UP.negate().add(RIGHT)),
        VirtualizationUtil.forwardDifference(List.of(ZERO, UP, RIGHT), VECTOR_DIFF));

    assertEquals(
        List.of(UP, RIGHT),
        VirtualizationUtil.forwardDifference(List.of(ZERO, UP, RIGHT.add(UP)), VECTOR_DIFF));

    // vector field
    assertEquals(
        List.of(List.of(UP, UP)),
        VirtualizationUtil.forwardDifference(
            List.of(List.of(ZERO, RIGHT), List.of(UP, RIGHT.add(UP))), FIELD_DIFF));
  }

  private static class Vector {
    private final int x;
    private final int y;

    private Vector(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public Vector add(Vector other) {
      return new Vector(this.x + other.x, this.y + other.y);
    }

    public Vector negate() {
      return new Vector(-this.x, -this.y);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Vector) {
        Vector other = (Vector) o;
        return this.x == other.x && this.y == other.y;
      }
      return false;
    }

    @Override
    public String toString() {
      return String.format("x: %d, y: %d", this.x, this.y);
    }
  }
}
