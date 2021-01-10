load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "jmh",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectProfiler.java",
    "EflectResult.java",
  ],
  deps = [
    "@eflect//java/eflect/testing",
    "@clerk//java/clerk:fixed_period",
    "@clerk//java/clerk/data:processors",
    "@org_openjdk_jmh_jmh_core",
  ],
)
