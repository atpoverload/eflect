load("@rules_java//java:defs.bzl", "java_library", "java_binary")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "chappie",
  visibility = ["//visibility:public"],
  srcs = [
    "Eflect.java"
  ],
  deps = [
    "//profiler/chappie/data:module",
    "//profiler/chappie/processing:module",
    "//profiler/eflect:footprint",
    "//profiler/eflect/data:module",
    "@clerk//clerk/util:logger",
    "@clerk//clerk/inject:module",
    "@clerk//clerk/inject:periodic_sampling_module",
    ":dagger",
  ],
  exports = [
    "//profiler/eflect:footprint",
  ],
  resources = [
    "@async_profiler//:async-profiler-lib",
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ],
)

java_binary(
  name = "chappie_profiler",
  visibility = ["//visibility:public"],
  main_class = "chappie.Eflect",
  runtime_deps = [
    ":chappie",
  ],
)
