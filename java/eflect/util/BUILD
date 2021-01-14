load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "util",
  visibility = ["//visibility:public"],
  srcs = glob(
    ["*.java"],
    exclude = ["AsyncProfilerUtil.java", "ProcUtil.java"]
  ),
)

java_library(
  name = "proc",
  visibility = ["//visibility:public"],
  srcs = ["ProcUtil.java"],
  deps = [
    "@net_java_dev_jna_jna",
  ],
)
