load("@rules_java//java:defs.bzl", "java_library", "java_binary")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = [
    "EnergyFootprint.java",
    "EflectProcessor.java",
    "EflectSampleMerger.java",
  ],
  deps = [
    "//profiler/eflect/data",
    "//profiler/eflect/utils",
    "@clerk//clerk",
    "@jRAPL//src/java/jrapl",
  ]
)

java_library(
  name = "module",
  srcs = [
    "EflectModule.java"
  ],
  deps = [
    ":eflect",
    "//profiler/eflect/data",
    "@clerk//clerk",
    ":dagger",
  ]
)

java_library(
  name = "profiler",
  visibility = ["//visibility:public"],
  srcs = [
    "Eflect.java"
  ],
  deps = [
    ":eflect",
    ":module",
    "//profiler/eflect/data",
    "//profiler/eflect/utils",
    "@clerk//clerk:profiler",
    "@clerk//clerk/concurrent",
    "@clerk//clerk/concurrent:periodic_sampling",
    ":dagger",
  ]
)

java_binary(
  name = "eflect_it",
  visibility = ["//visibility:public"],
  main_class = "eflect.Eflect",
  runtime_deps = [
    ":profiler",
  ],
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ],
)
