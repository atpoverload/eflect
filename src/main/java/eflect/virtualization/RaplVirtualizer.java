package eflect.virtualization;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import eflect.EflectDataSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import jrapl.Powercap;
import jrapl.RaplDifference;
import jrapl.RaplReading;
import jrapl.RaplSample;

public final class RaplVirtualizer {
  /** Tries to align the {@link TaskSamples} and {@link CpuSamples} into jiffy virtualizations. */
  public static List<VirtualizedEnergy> virtualize(
      EflectDataSet data, IntUnaryOperator socketMapper, long millisThresh) {
    if (data.getRaplCount() < 2) {
      System.out.println("not enough energy samples to align data");
      return new ArrayList<>();
    }

    List<Difference<Activity>> activities = JiffiesVirtualizer.virtualize(data, millisThresh);

    if (activities.isEmpty()) {
      System.out.println("no activity virtualization");
      return new ArrayList<>();
    }

    Iterator<RaplDifference> energies = processRapl(data.getRaplList()).iterator();
    Iterator<Difference<Activity>> activityIt = activities.iterator();

    Difference<Activity> activity = activityIt.next();
    RaplDifference energy = energies.next();

    ArrayList<VirtualizedEnergy> virtualized = new ArrayList<>();
    while (true) {
      long activityStart = Timestamps.toMillis(activity.start) / millisThresh;
      long activityEnd = Timestamps.toMillis(activity.end) / millisThresh;
      long energyStart = Timestamps.toMillis(energy.getStart()) / millisThresh;
      long energyEnd = Timestamps.toMillis(energy.getEnd()) / millisThresh;

      if (energyStart < activityStart) {
        if (!energies.hasNext()) {
          break;
        }
        energy = energies.next();
        continue;
      } else if (activityEnd < energyEnd) {
        if (!activityIt.hasNext()) {
          break;
        }
        activity = activityIt.next();
        continue;
      }

      virtualized.addAll(
        virtualizeEnergy(
              Timestamps.fromMillis(
                  millisThresh * (activityStart < energyStart ? activityStart : energyStart)),
              Timestamps.fromMillis(
                  millisThresh * (activityEnd > energyEnd ? activityEnd : energyEnd)),
              activity, energy, socketMapper));

      if (!energies.hasNext() || !activityIt.hasNext()) {
        break;
      }

      energy = energies.next();
      activity = activityIt.next();

      break;
    }

    return virtualized;
  }

  /** Sort the samples by timestamp and compute the forward difference between pairs. */
  private static List<RaplDifference> processRapl(List<RaplSample> samples) {
    ArrayList<RaplDifference> diffs = new ArrayList<>();
    Optional<RaplSample> last = Optional.empty();
    for (RaplSample sample :
        samples
            .stream()
            .sorted((s1, s2) -> Timestamps.compare(s1.getTimestamp(), s2.getTimestamp()))
            .collect(toList())) {
      if (last.isPresent()) {
        diffs.add(Powercap.difference(last.get(), sample));
      }
      last = Optional.of(sample);
    }
    return diffs;
  }

  /** Compute the jiffies consumption of tasks with task jiff / cpu jiff / total task jiff. */
  private static List<VirtualizedEnergy> virtualizeEnergy(
      Timestamp start, Timestamp end, Difference<Activity> task, RaplDifference energy, IntUnaryOperator socketMapper) {
    Map<Integer, RaplReading> readings =
        energy.getReadingList().stream().collect(toMap(r -> r.getSocket(), r -> r));
    return task.data
        .stream()
        .filter(a -> a.activity > 0)
        .map(
            r -> {
              RaplReading reading = readings.get(socketMapper.applyAsInt(r.cpu));
              return VirtualizedEnergy.newBuilder()
                  .setStart(start)
                  .setEnd(end)
                  .setEnergy(
                      (reading.getPackage()
                              + reading.getDram()
                              + reading.getCore()
                              + reading.getGpu())
                          * r.activity)
                  .setId(r.id)
                  .build();
            })
        .collect(toList());
  }

  private RaplVirtualizer() {}
}
