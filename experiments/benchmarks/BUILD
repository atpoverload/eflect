load("@rules_java//java:defs.bzl", "java_library")
load("//:eflect_experiment_defs.bzl", "eflect_jmh_benchmark")

java_library(
  name = "profiler",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectProfiler.java",
    "EflectResult.java",
  ],
  deps = [
    "@eflect//java/eflect:linux_eflect",
    "@eflect//java/eflect/data",
    "@org_openjdk_jmh_jmh_core",
  ],
  exports = [
    "@org_openjdk_jmh_jmh_core",
  ]
)

eflect_jmh_benchmark(
  name = "eflect",
  profiler_class = "eflect.benchmarks.EflectProfiler",
  srcs = [
    "EflectBenchmark.java",
  ],
  deps = [
    ":profiler",
    "//jmh:fake_linux",
  ]
)
