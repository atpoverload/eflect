package eflect.data;

import static org.junit.Assert.assertEquals;

import eflect.testing.data.FakeAccountant;
import java.time.Instant;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StackTraceAlignerTest {
  private FakeAccountant<EnergyFootprint> accountant;
  private StackTraceAligner aligner;

  @Before
  public void setUp() {
    accountant = new FakeAccountant<>();
    aligner = new StackTraceAligner(accountant);
  }

  @After
  public void tearDown() {
    accountant = null;
    aligner = null;
  }

  @Test
  public void process() {
    EnergyFootprint expected =
        new EnergyFootprint.Builder()
            .setName("fake-thread")
            .setId(1)
            .setStart(Instant.EPOCH)
            .setEnd(Instant.MAX)
            .setEnergy(1)
            .build();
    accountant.setData(List.of(expected));
    aligner.add(new StackTraceSample(Instant.EPOCH, 1, "FAKE-TRACE"));
    EnergyFootprint actual = ((List<EnergyFootprint>) aligner.process()).get(0);
    assertEquals(expected.id, actual.id);
    assertEquals("FAKE-TRACE", actual.stackTrace);
  }
}
