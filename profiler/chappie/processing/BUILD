load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "processing",
  visibility = ["//visibility:public"],
  srcs = glob(
    ["*.java"],
    exclude = ["ChappieProcessingModule.java"],
  ),
  deps = [
    "//profiler/chappie/data",
    "//profiler/eflect:footprint",
    "//profiler/eflect/data",
    "//profiler/eflect/processing",
    "//profiler/eflect/util",
    "@clerk//clerk",
  ],
)


java_library(
  name = "module",
  visibility = ["//visibility:public"],
  srcs = ["ChappieProcessingModule.java"],
  deps = [
    ":processing",
    "@clerk//clerk",
    ":dagger",
  ],
)
