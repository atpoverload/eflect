load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "collector",
  visibility = ["//visibility:public"],
  srcs = ["EflectSampleCollector.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/async:sources",
    "//java/eflect/data/jiffies:sources",
    "//java/eflect/data/rapl:sources",
  ],
  exports = [
    "//java/eflect/data",
    "//java/eflect/data/async:sources",
    "//java/eflect/data/jiffies:sources",
    "//java/eflect/data/rapl:sources",
  ]
)

java_binary(
  name = "monitor",
  visibility = ["//visibility:public"],
  main_class = "eflect.EflectProcessMonitor",
  srcs = ["EflectProcessMonitor.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/jiffies:sources",
    "//java/eflect/data/rapl:sources",
    "//java/eflect/util",
  ],
)
