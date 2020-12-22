load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_binary(
  name = "eflect",
  visibility = ["//visibility:public"],
  main_class = "eflect.Eflect",
  srcs = glob(["*.java"]),
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
