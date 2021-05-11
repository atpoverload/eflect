load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "jiffies",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"], exclude = ["ProcDataSources.java"]),
  deps = [
    "//java/eflect/data",
  ],
)

java_library(
  name = "sources",
  visibility = ["//visibility:public"],
  srcs = ["ProcDataSources.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/jiffies",
    "//java/eflect/util",
  ],
)
