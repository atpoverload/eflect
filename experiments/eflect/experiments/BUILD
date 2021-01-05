load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "//java/eflect",
    "//java/eflect/util",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/eflect"],
  args = ["-c eflect.experiments.DaCapo"]
)

java_binary(
  name = "tensorflow",
  visibility = ["//visibility:public"],
  main_class = "eflect.experiments.Tensorflow",
  srcs = [
    "Tensorflow.java",
  ],
  deps = [
    "//java/eflect",
    "//java/eflect/util",
    "@tensorflow//tensorflow/java",
  ],
  jvm_flags = ["-Deflect.output=/mnt/c/Users/Timur/Documents/projects/eflect"],
)
