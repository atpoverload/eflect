package eflect.data;

import eflect.util.TimeUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/** Processor that merges samples into task energy footprints. */
// TODO(timur): add(), account(), and discard() have races with each other
public final class EnergyAccountant implements Accountant<Collection<EnergyFootprint>> {
  private final int domainCount;
  private final int componentCount;
  private final double wrapAround;
  private final Accountant<Collection<ThreadActivity>> activityAccountant;
  private final double[][] energyMin;
  private final double[][] energyMax;

  private Instant start = Instant.MAX;
  private Instant end = Instant.MIN;

  public EnergyAccountant(
      int domainCount,
      int componentCount,
      double wrapAround,
      Accountant<Collection<ThreadActivity>> activityAccountant) {
    this.domainCount = domainCount;
    this.componentCount = componentCount;
    this.wrapAround = wrapAround;
    this.activityAccountant = activityAccountant;
    // TODO(timur): this may change if we come up with a formal definition for domains + components
    // TODO(timur): are we missing energy because it gets globbed?
    energyMin = new double[domainCount][componentCount];
    energyMax = new double[domainCount][componentCount];
    for (int domain = 0; domain < domainCount; domain++) {
      Arrays.fill(energyMin[domain], -1);
      Arrays.fill(energyMax[domain], -1);
    }
  }

  /** Put the sample data into the correct container. */
  @Override
  public void add(Sample s) {
    if (s instanceof EnergySample) {
      addEnergy(((EnergySample) s).getEnergy());
    } else {
      activityAccountant.add(s);
    }
    // need to know the range of time for samples
    Instant timestamp = s.getTimestamp();
    start = TimeUtil.min(timestamp, start);
    end = TimeUtil.max(timestamp, end);
  }

  /** Add all samples from the other accountant if it is a {@link JiffiesAccountant}. */
  @Override
  public <T extends Accountant<Collection<EnergyFootprint>>> void add(T o) {
    if (o instanceof EnergyAccountant) {
      EnergyAccountant other = (EnergyAccountant) o;
      addEnergy(other.energyMin);
      addEnergy(other.energyMax);
      activityAccountant.add(other.activityAccountant);
      start = TimeUtil.min(start, other.start);
      end = TimeUtil.max(end, other.end);
    }
  }

  /**
   * Attempts to account the stored data.
   *
   * <p>Returns Result.UNACCOUNTABLE if a domain has no energy.
   *
   * <p>Returns the result of the {@link ActivityAccountant} otherwise.
   */
  // TODO(timur): synchronize with add
  @Override
  public Accountant.Result account() {
    // check the timestamps
    if (TimeUtil.equal(start, end)
        || TimeUtil.equal(start, Instant.MAX)
        || TimeUtil.equal(end, Instant.MIN)) {
      return Accountant.Result.UNACCOUNTABLE;
    }

    // check the energy
    for (int domain = 0; domain < domainCount; domain++) {
      for (int component = 0; component < componentCount; component++) {
        if (energyMax[domain][component] < 0
            || energyMin[domain][component] < 0
            || energyMax[domain][component] == energyMin[domain][component]) {
          return Accountant.Result.UNACCOUNTABLE;
        }
      }
    }

    return activityAccountant.account();
  }

  /** Returns the data if it's accountable. Otherwise, return an empty list. */
  // TODO(timur): synchronize with add
  @Override
  public Collection<EnergyFootprint> process() {
    if (account() != Accountant.Result.UNACCOUNTABLE) {
      ArrayList<EnergyFootprint> footprints = new ArrayList<>();
      double[] energy = new double[domainCount];
      for (int domain = 0; domain < domainCount; domain++) {
        for (int component = 0; component < componentCount; component++) {
          double componentEnergy = energyMax[domain][component] - energyMin[domain][component];
          if (energy[domain] < 0) {
            componentEnergy += wrapAround;
          }
          energy[domain] += componentEnergy;
        }
      }
      for (ThreadActivity thread : activityAccountant.process()) {
        double taskEnergy = thread.activity * energy[thread.domain];
        footprints.add(
            new EnergyFootprint.Builder()
                .setId(thread.id)
                .setName(thread.name)
                .setStart(start)
                .setEnd(end)
                .setEnergy(taskEnergy)
                .build());
      }
      return footprints;
    } else {
      return new ArrayList<EnergyFootprint>();
    }
  }

  /** Sets the min values to the max values. */
  // TODO(timur): synchronize with add
  @Override
  public void discardStart() {
    start = end;
    for (int domain = 0; domain < domainCount; domain++) {
      for (int component = 0; component < componentCount; component++) {
        energyMin[domain][component] = energyMax[domain][component];
      }
    }
    activityAccountant.discardStart();
  }

  /** Sets the max values to the min values. */
  // TODO(timur): synchronize with add
  @Override
  public void discardEnd() {
    end = start;
    for (int domain = 0; domain < domainCount; domain++) {
      for (int component = 0; component < componentCount; component++) {
        energyMax[domain][component] = energyMin[domain][component];
      }
    }
    activityAccountant.discardEnd();
  }

  private void addEnergy(double[][] energy) {
    for (int domain = 0; domain < domainCount; domain++) {
      for (int component = 0; component < componentCount; component++) {
        // TODO(timur): are we missing energy because it gets globbed?
        double componentEnergy = energy[domain][component];
        if (componentEnergy < 0) {
          continue;
        }
        if (energyMin[domain][component] < 0 || componentEnergy < energyMin[domain][component]) {
          energyMin[domain][component] = componentEnergy;
        }
        if (componentEnergy > energyMax[domain][component]) {
          energyMax[domain][component] = componentEnergy;
        }
      }
    }
  }
}
