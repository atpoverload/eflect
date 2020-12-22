package eflect.data;

import java.time.Instant;
import java.util.TreeMap;
import java.util.function.Supplier;

/** Processor that merges accountants into a footprint with task granularity. */
public final class VanillaEflectProcessor extends EflectProcessor {
  private final TreeMap<Instant, EnergyAccountant> data = new TreeMap<>();
  private final Supplier<EnergyAccountant> accountantFactory;

  public VanillaEflectProcessor(Supplier<EnergyAccountant> accountantFactory) {
    this.accountantFactory = accountantFactory;
  }

  @Override
  public EnergyAccountant newAccountant() {
    return accountantFactory.get();
  }
}
