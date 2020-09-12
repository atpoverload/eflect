package eflect;

public class Driver {
  public static void main(String[] args) throws Exception {
    EflectProfiler.start();
    for (int i = 0; i < 1; i++) {
      Thread.sleep(500);
      // for (EnergyFootprint e: EflectProfiler.dump()) {
      //   System.out.println(e.getEnergy());
      // }
    }
    EflectProfiler.stop();

    for (EnergyFootprint e: EflectProfiler.dump()) {
      System.out.println(e.getStart() + "," + e.getEnd() + "," + e.getEnergy() + "," + e.getPower());
    }

    // for (EnergyFootprint e: EflectProfiler.dump()) {
    //   System.out.println(e.getEnergy());
    // }
  }
}
