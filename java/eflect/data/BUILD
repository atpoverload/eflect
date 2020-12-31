load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "data",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"]),
  deps = [
    "//java/eflect/util",
    "@clerk//java/clerk",
  ]
)
