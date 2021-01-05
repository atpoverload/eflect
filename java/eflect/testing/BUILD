load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "testing",
  visibility = ["//visibility:public"],
  srcs = ["DummyEflect.java"],
  deps = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/testing/data",
    "//java/eflect/util:proc",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
  exports = [
    "//java/eflect/data",
    "//java/eflect/data/async",
    "//java/eflect/data/jiffies",
    "//java/eflect/testing/data",
    "//java/eflect/util:proc",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
  ],
)

java_binary(
  name = "driver",
  visibility = ["//visibility:private"],
  main_class = "eflect.testing.DummyEflectDriver",
  srcs = ["DummyEflectDriver.java"],
  deps = [
    ":testing",
    "//java/eflect/util",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/eflect"],
)
