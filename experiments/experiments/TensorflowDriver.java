package eflect.experiments;

import static eflect.experiments.util.TensorFlowUtil.normalizeImage;
import static eflect.experiments.util.TensorFlowUtil.readBytes;

import java.nio.file.Paths;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/** Runs a frozen tensorflow graph on image data. */
public class TensorflowDriver {
  private static void checkArgs(String[] args) {
    if (args.length < 1) {
      System.out.println("Expected 2 args; got " + (args.length - 1) + ": no model graph provided");
      System.exit(1);
    } else if (args.length < 2) {
      System.out.println(
          "Expected 2 args; got " + (args.length - 1) + ": no evaluation image provided");
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    checkArgs(args);
    // hack to load the stripped .so
    System.load(System.getProperty("tf.lib"));
    try (Tensor<?> data = normalizeImage(readBytes(Paths.get(args[1]))); ) {
      executeGraph(readBytes(Paths.get(args[0])), data);
    }
  }

  private static void executeGraph(byte[] graphDef, Tensor<?> data) {
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      int iters = 20;
      int batches = 1250;
      EflectMonitor.getInstance().start();
      for (int i = 0; i < iters * batches; i++) {
        try (Session s = new Session(g)) {
          List<Tensor<?>> result =
              s.runner().feed("input", data).fetch("InceptionV3/Predictions/Reshape/shape").run();
        }
        if ((i + 1) % (batches) == 0) {
          System.out.println("completed iteration " + Integer.toString(batches / i));
          EflectMonitor.getInstance().stop();
          EflectMonitor.getInstance().dump();
          EflectMonitor.getInstance().start();
        }
      }
    }
  }

  private TensorflowDriver() {}
}
