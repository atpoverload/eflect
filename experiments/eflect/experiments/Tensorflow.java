package eflect.experiments;

import eflect.Eflect;
import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.Signature;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.op.math.Add;
import org.tensorflow.types.TInt32;

public class Tensorflow {
  private static Signature dbl(Ops tf) {
    Placeholder<TInt32> x = tf.placeholder(TInt32.DTYPE);
    Add<TInt32> dblX = tf.math.add(x, x);
    return Signature.builder().input("x", x).output("dbl", dblX).build();
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Hello TensorFlow " + TensorFlow.version());

    System.out.println("starting eflect");
    Eflect eflect = new Eflect(Duration.ofMillis(41));

    try (ConcreteFunction dbl = ConcreteFunction.create(HelloTensorFlow::dbl);
        Tensor<TInt32> x = TInt32.scalarOf(10);
        Tensor<TInt32> dblX = dbl.call(x).expect(TInt32.DTYPE)) {
      System.out.println(x.data().getInt() + " doubled is " + dblX.data().getInt());
    }

    eflect.stop();
    System.out.println("stopped eflect");
    WriterUtils.writeCsv(
        new File(System.getProperty("eflect.output", "."), "eflect-footprints.csv").getPath(),
        "id,name,start,end,energy,trace",
        eflect.read());
    eflect.terminate();
  }
}
