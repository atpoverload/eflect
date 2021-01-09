load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = ["Eflect.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/util:async",
    "//java/eflect/util:proc",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
    "@jRAPL//src/java/jrapl",
  ],
  exports = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  resources = [
    "@async_profiler//:async-profiler-lib",
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ]
)

java_library(
  name = "freq",
  visibility = ["//visibility:public"],
  srcs = ["CpuFreqMonitor.java"],
  deps = [
    "//java/eflect/data",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  exports = [
    "//java/eflect/data",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
)

java_binary(
  name = "freq_test",
  visibility = ["//visibility:public"],
  main_class = "eflect.CpuFreqMonitor",
  runtime_deps = [
    ":freq",
  ],
)
