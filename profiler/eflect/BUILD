load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "eflect",
  visibility = ["//visibility:public"],
  main_class = "eflect.Driver",
  srcs = [
    "Driver.java",
  ],
  deps = [
    "//src/eflect",
    "//src/eflect:profiler",
  ],
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ],
)
