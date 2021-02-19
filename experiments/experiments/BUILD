load("@rules_java//java:defs.bzl", "java_library", "java_binary")

java_library(
  name = "monitor",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectCalmnessMonitor.java",
  ],
  deps = [
    "@eflect//java/eflect:freq",
    "@eflect//java/eflect:linux_eflect",
    "@eflect//java/eflect/util",
    "@jRAPL//src/java/jrapl",
  ]
)

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    ":monitor",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/data/dacapo"],
  args = ["-c eflect.experiments.DaCapo --no-validation"]
)

java_binary(
  name = "tensorflow",
  visibility = ["//visibility:public"],
  main_class = "eflect.experiments.TensorflowDriver",
  srcs = [
    "TensorflowDriver.java",
  ],
  deps = [
    ":monitor",
    "//experiments/util",
    "@eflect//java/eflect/util",
    "@tensorflow",
  ],
  jvm_flags = [
    "-Deflect.output=/home/timur/projects/data/tensorflow",
    "-Dtf.lib=/home/timur/projects/eflect/experiments/tensorflow/bazel-bin/tensorflow/java/libtensorflow_jni.so",
  ]
)

java_binary(
  name = "tensorflow_hub",
  visibility = ["//visibility:public"],
  main_class = "eflect.experiments.TensorflowSavedModelDriver",
  srcs = [
    "TensorflowSavedModelDriver.java",
  ],
  deps = [
    ":monitor",
    "//experiments/util",
    "@eflect//java/eflect/util",
    "@tensorflow",
  ],
  jvm_flags = [
    "-Deflect.output=/home/timur/projects/data/tensorflow",
    "-Dtf.lib=/home/timur/projects/eflect/experiments/tensorflow/bazel-bin/tensorflow/java/libtensorflow_jni.so",
  ]
)
