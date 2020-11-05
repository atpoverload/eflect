load("@rules_java//java:defs.bzl", "java_library", "java_binary")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "footprint",
  visibility = ["//visibility:public"],
  srcs = [
    "EnergyFootprint.java",
  ],
)

java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = [
    "Eflect.java"
  ],
  deps = [
    ":footprint",
    "//profiler/eflect/data:module",
    "//profiler/eflect/processing:module",
    "@clerk//clerk/util",
    "@clerk//clerk/inject:clerk",
    "@clerk//clerk/inject:executor_module",
    ":dagger",
  ],
  exports = [
    ":footprint",
  ],
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ],
)

java_binary(
  name = "eflect_profiler",
  visibility = ["//visibility:public"],
  main_class = "eflect.Eflect",
  runtime_deps = [
    ":eflect",
  ],
)
