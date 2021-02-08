/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package eflect.experiments;

import static eflect.experiments.util.TensorFlowUtil.normalizeImage;
import static eflect.experiments.util.TensorFlowUtil.readBytes;

import java.nio.file.Paths;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/** Sample use of the TensorFlow Java API to label images using a pre-trained model. */
public class TfDriver {
  private static checkArgs() {
    if (args.length < 1) {
      System.out.println("Expected 2 args; got " + (args.length - 1) + ": no model graph provided");
      System.exit(1);
    } else if (args.length < 2) {
      System.out.println(
          "Expected 2 args; got " + (args.length - 1) + ": no evaluation data provided");
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    // hack to load the stripped .so
    checkArgs();
    System.load(System.getProperty("tf.lib"));
    try (Tensor<Float> image = normalizeImage(readBytes(Paths.get(args[1]))); ) {
      executeGraph(readBytes(Paths.get(args[0])), image, "output");
    }
  }

  private static void executeGraph(byte[] graphDef, Tensor<Float> image, String outputName) {
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      EflectCalmnessMonitor.getInstance().start(41);
      for (int i = 0; i < 250; i++) {
        try (Session s = new Session(g);
            Tensor<Float> result =
                s.runner()
                    .feed("input", image)
                    .fetch(outputName)
                    .run()
                    .get(0)
                    .expect(Float.class)) {}
      }
      EflectCalmnessMonitor.getInstance().stop();
      EflectCalmnessMonitor.getInstance().dump(benchmark, Integer.toString(iteration++));
    }
  }
}
