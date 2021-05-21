load("@rules_java//java:defs.bzl", "java_binary", "java_library")

genrule(
  name = "lib-async-profiler",
  visibility = ["//visibility:private"],
  srcs = ["@async-profiler//:lib-async-profiler"],
  outs = ["libasyncProfiler.so"],
  cmd = "cp -f $< $@",
)

genrule(
  name = "lib-cpu-scaler",
  visibility = ["//visibility:private"],
  srcs = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  outs = ["libCPUScaler.so"],
  cmd = "cp -f $< $@",
)

java_library(
  name = "sample_collector",
  visibility = ["//visibility:public"],
  srcs = ["EflectSampleCollector.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/async:sources",
    "//java/eflect/data/jiffies:sources",
    "//java/eflect/util",
    "@clerk//java/clerk/storage",
    "@clerk//java/clerk/util",
    "@jRAPL//src/java/jrapl",
  ],
  exports = [
    "//java/eflect/data",
    "@clerk//java/clerk/storage",
    "@clerk//java/clerk/util",
  ],
  data = [
    ":lib-cpu-scaler",
    ":lib-async-profiler",
  ]
)
