load("@rules_java//java:defs.bzl", "java_library", "java_binary")

java_library(
  name = "monitor",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectMonitor.java",
  ],
  deps = [
    "@eflect//java/eflect:sample_collector",
    "@eflect//java/eflect/util",
    "@jRAPL//src/java/jrapl",
    "@clerk//java/clerk",
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
    "//java/eflect/experiments/util",
    "@tensorflow",
  ],
  jvm_flags = [
    "-Dtf.lib=/home/timur/projects/eflect/experiments/tensorflow/bazel-bin/tensorflow/java/libtensorflow_jni.so",
  ]
)
