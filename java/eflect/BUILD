load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "dummy",
  visibility = ["//visibility:public"],
  srcs = ["DummyEflect.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/testing/data",
    "//java/eflect/util",
    "//java/eflect/util:proc",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  exports = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/testing/data",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ]
)

java_binary(
  name = "dummy_driver",
  visibility = ["//visibility:public"],
  main_class = "eflect.DummyEflect",
  runtime_deps = [
    ":dummy",
  ]
)

java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = ["Eflect.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/data/rapl",
    "//java/eflect/util",
    "//java/eflect/util:async",
    "//java/eflect/util:proc",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
    "@jRAPL//src/java/jrapl",
  ],
  exports = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/data/rapl",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ]
)

java_binary(
  name = "driver",
  visibility = ["//visibility:public"],
  main_class = "eflect.Eflect",
  runtime_deps = [
    ":eflect",
  ]
)
