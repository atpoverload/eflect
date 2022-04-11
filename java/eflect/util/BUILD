load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "util",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"], exclude = ["LoggerUtil.java"]),
)

java_library(
  name = "logger",
  visibility = ["//visibility:public"],
  srcs = ["LoggerUtil.java"],
)
