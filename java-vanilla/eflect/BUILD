load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "dummy",
  visibility = ["//visibility:public"],
  srcs = ["DummyEflect.java"],
  deps = [
    "//java-core/eflect/data",
    "//java-core/eflect/data/jiffies",
    "//java-core/eflect/testing/data",
    "//java-core/eflect/util",
    "//java-vanilla/eflect/data",
    "//java-vanilla/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  exports = [
    "//java-core/eflect/data",
    "//java-core/eflect/data/jiffies",
    "//java-core/eflect/testing/data",
    "//java-vanilla/eflect/data",
    "//java-vanilla/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ]
)


java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = ["Eflect.java"],
  deps = [
    "//java-core/eflect/data",
    "//java-core/eflect/data/jiffies",
    "//java-core/eflect/data/rapl",
    "//java-core/eflect/util",
    "//java-vanilla/eflect/data",
    "//java-vanilla/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
    "@jRAPL//src/java/jrapl",
  ],
  exports = [
    "//java-core/eflect/data",
    "//java-core/eflect/data/jiffies",
    "//java-core/eflect/data/rapl",
    "//java-vanilla/eflect/data",
    "//java-vanilla/eflect/data/jiffies",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ]
)
