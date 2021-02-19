package eflect.experiments;

import static eflect.util.LoggerUtil.getLogger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

/** Runs a tensorflow graph from a SavedModel on data. */
public class TensorflowSavedModelDriver {
  private static checkArgs() {
    if (args.length < 1) {
      System.out.println("Expected 1 args; got " + (args.length - 1) + ": no path to SavedModel");
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    checkArgs();
    // hack to load the stripped .so
    System.load(System.getProperty("tf.lib"));
    // try (Tensor<?> data = normalizeImage(readBytes(Paths.get(args[1]))); ) {
    // try (Tensor<?> data = Tensor.create(new byte[][]{"hello there!".getBytes()})) {
    try (Tensor<?> data = Tensor.create("hello there!".getBytes())) {
      executeGraph(SavedModelBundle.load(args[0], "serve"), data);
    }
  }

  private static void executeGraph(SavedModelBundle bundle, Tensor<?> data) throws Exception {
    EflectCalmnessMonitor.getInstance().start(41);
    for (int i = 0; i < 250; i++) {
      try (Tensor<?> temp = Tensor.create(new int[][] {{1, 1}, {1, 1}})) {
        List<Tensor<?>> result =
            bundle
                .session()
                .runner()
                .feed("saver_filename", data)
                // .feed("serving_default_input_mask", temp)
                // .feed("serving_default_input_type_ids", temp)
                // .feed("serving_default_input_word_ids", temp)
                .fetch("StatefulPartitionedCall")
                .run();
        TimeUnit.MILLISECONDS.sleep(100);
        if ((i + 1) % 25 == 0) {
          getLogger().info("completed iter " + Integer.toString(i + 1));
        }
      }
    }
    EflectCalmnessMonitor.getInstance().stop();
    EflectCalmnessMonitor.getInstance().dump("resnet");
  }

  private TensorflowSavedModelDriver() {}
}
