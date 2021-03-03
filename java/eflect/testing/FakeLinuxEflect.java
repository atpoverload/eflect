package eflect.testing;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.Eflect;
import eflect.data.EnergySample;
import eflect.data.Sample;
import eflect.data.SampleCollection;
import eflect.data.StackTraceSample;
import eflect.data.jiffies.JiffiesAccountant;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that provides fake application energy for an intel linux system. */
public final class FakeLinuxEflect extends Eflect {
  private static final long PID = ProcessHandle.current().pid();
  private static final Supplier<String> traceGenerator =
      () -> Integer.toString((int) (Math.random() * 1000000));

  private static String[] listTasks() {
    return new File(String.join(File.separator, "/proc", Long.toString(PID), "task")).list();
  }

  private static class FakeStackTraceSampleCollection implements SampleCollection {
    private final ArrayList<Sample> samples = new ArrayList<>();

    private FakeStackTraceSampleCollection() {}

    public void addSample(StackTraceSample sample) {
      samples.add(sample);
    }

    @Override
    public Instant getTimestamp() {
      return Instant.EPOCH;
    }

    @Override
    public Collection<Sample> getSamples() {
      return samples;
    }
  }

  private static Collection<Supplier<?>> getSources() {
    long start = System.currentTimeMillis();
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl =
        () ->
            new EnergySample(Instant.now(), new double[][] {{System.currentTimeMillis() - start}});
    Supplier<?> async =
        () -> {
          FakeStackTraceSampleCollection samples = new FakeStackTraceSampleCollection();
          for (String id : listTasks()) {
            samples.addSample(
                new StackTraceSample(Instant.now(), Long.parseLong(id), traceGenerator.get()));
          }
          return samples;
        };
    return List.of(stat, task, rapl, async);
  }

  public FakeLinuxEflect(ScheduledExecutorService executor, Duration period) {
    super(getSources(), 1, 1, 0, () -> new JiffiesAccountant(1, cpu -> 0), 100, executor, period);
  }

  public static void main(String[] args) throws Exception {
    ScheduledExecutorService executor = newScheduledThreadPool(4);
    FakeLinuxEflect eflect = new FakeLinuxEflect(executor, Duration.ofMillis(41));
    eflect.start();
    Thread.sleep(10000);
    eflect.stop();
    System.out.println(eflect.read());
    executor.shutdown();
  }
}
