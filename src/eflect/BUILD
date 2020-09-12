load("@rules_java//java:defs.bzl", "java_library")
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
    "//src/eflect/data",
    "//src/eflect/utils",
    "@clerk//core/clerk",
    "@jRAPL"
  ]
)

java_library(
  name = "module",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectModule.java"
  ],
  deps = [
    ":eflect",
    "//src/eflect/data",
    "@clerk//core/clerk",
    ":dagger",
  ]
)

java_library(
  name = "profiler",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectProfiler.java"
  ],
  deps = [
    ":eflect",
    ":module",
    "//src/eflect/data",
    "@clerk//core/clerk",
    "@clerk//core/clerk:profiler",
    "@clerk//core/clerk/concurrent",
    "@clerk//core/clerk/concurrent:periodic_scheduler",
    "@clerk//core/clerk/concurrent:periodic_scheduling",
    ":dagger",
  ]
)
