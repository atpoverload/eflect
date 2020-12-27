load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "//java-core/eflect/data",
    "//java-core/eflect/data/rapl",
    "//java-core/eflect/testing/data",
    "//java-vanilla/eflect:dummy",
    "//java-vanilla/eflect/data",
    "//java-vanilla/eflect/data/jiffies",
    "@dacapo",
    "@clerk//java/clerk",
    "@clerk//java/clerk:fixed_period",
    "@clerk//java/clerk/util",
  ],
  resources = [
    "@jRAPL//src/jrapl:libCPUScaler.so",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/sandbox/eflect/data"],
)
