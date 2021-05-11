load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "async",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"], exclude = ["AsyncProfilerDataSources.java"]),
  deps = [
    "//java/eflect/data",
  ],
)

java_library(
  name = "sources",
  visibility = ["//visibility:public"],
  srcs = ["AsyncProfilerDataSources.java"],
  deps = [
    ":async",
    "//java/eflect/data",
    "@async-profiler",
  ],
)
