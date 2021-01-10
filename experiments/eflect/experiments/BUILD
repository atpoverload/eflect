load("@rules_java//java:defs.bzl", "java_binary")
load("//:eflect_experiment_defs.bzl", "eflect_benchmark")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "@eflect//java/eflect",
    "@eflect//java/eflect:freq",
    "@eflect//java/eflect/util",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/eflect/data"],
  args = ["-c eflect.experiments.DaCapo"]
)

eflect_benchmark(
  name = "benchmark1",
  srcs = [
    "JMHBenchmark1.java",
  ],
)
