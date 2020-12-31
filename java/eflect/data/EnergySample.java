package eflect.data;

/** Sample of consumed global energy. */
public interface EnergySample extends Sample {
  /** Return the energy broken down by domain and component. */
  // TODO(timur): eventually we have to think if the domains need to be further abstracted
  double[][] getEnergy();
}
