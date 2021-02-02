load("@rules_java//java:defs.bzl", "java_binary", "java_library")

genrule(
  name = "lib-async-profiler",
  visibility = ["//visibility:public"],
  srcs = ["@async-profiler//:lib-async-profiler"],
  outs = ["libasyncProfiler.so"],
  cmd = "cp -f $< $@",
)

genrule(
  name = "lib-cpu-scaler",
  visibility = ["//visibility:public"],
  srcs = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  outs = ["libCPUScaler.so"],
  cmd = "cp -f $< $@",
)

java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = ["Eflect.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  exports = [
    "//java/eflect/data",
    "//java/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ]
)

java_library(
  name = "linux_eflect",
  visibility = ["//visibility:public"],
  srcs = ["LinuxEflect.java"],
  deps = [
    ":eflect",
    "//java/eflect/data/async",
    "//java/eflect/util",
    "@async-profiler",
    "@jRAPL//src/java/jrapl",
  ],
  exports = [
    ":eflect",
    "//java/eflect/data/async",
  ],
  data = [
    ":lib-async-profiler",
    ":lib-cpu-scaler",
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
