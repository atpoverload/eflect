load("@rules_java//java:defs.bzl", "java_library", "java_binary")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "@eflect//java/eflect",
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
    "-Dtf.lib=/home/timur/projects/eflect/experiments/third_party/tensorflow/bazel-bin/tensorflow/java/libtensorflow_jni.so",
  ]
)
