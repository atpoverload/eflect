load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "chappie",
  visibility = ["//visibility:public"],
  srcs = [
    "StackTraceRanker.java",
  ],
  deps = [
    "//profiler/eflect",
    "//profiler/eflect/data",
    "//profiler/eflect/utils",
    "@clerk//clerk",
    ":dagger",
  ]
)

java_library(
  name = "module",
  srcs = [
    "StackTraceRankingModule.java",
  ],
  visibility = ["//visibility:public"],
  deps = [
    ":chappie",
    "//profiler/eflect/data",
    "@async_profiler",
    "@clerk//clerk",
    ":dagger",
  ],
)

java_binary(
  name = "rank_it",
  visibility = ["//visibility:public"],
  main_class = "chappie.ChappieProfiler",
  srcs = [
    "Chappie.java",
  ],
  deps = [
    ":module",
    "//profiler/eflect",
    "@async_profiler",
    "@clerk//clerk:profiler",
    "@clerk//clerk/concurrent",
    "@clerk//clerk/concurrent:periodic_sampling",
    ":dagger",
  ],
  resources = [
    "@async_profiler//:async-profiler-lib",
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ],
)
