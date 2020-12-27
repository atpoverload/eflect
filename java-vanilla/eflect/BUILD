load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_binary(
  name = "dummy",
  visibility = ["//visibility:public"],
  main_class = "eflect.DummyEflect",
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
)


java_binary(
  name = "eflect",
  visibility = ["//visibility:public"],
  main_class = "eflect.Eflect",
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
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ]
)
