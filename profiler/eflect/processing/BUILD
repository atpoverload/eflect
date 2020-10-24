load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "processing",
  visibility = ["//visibility:public"],
  srcs = glob(
    ["*.java"],
    exclude = ["EflectProcessingModule.java"]
  ),
  deps = [
    "//profiler/eflect:footprint",
    "//profiler/eflect/data",
    "//profiler/eflect/util",
    "@clerk//clerk",
    "@jRAPL//src/java/jrapl",
  ],
)

java_library(
  name = "module",
  visibility = ["//visibility:public"],
  srcs = ["EflectProcessingModule.java"],
  deps = [
    ":processing",
    "@clerk//clerk",
    ":dagger",
  ],
)
