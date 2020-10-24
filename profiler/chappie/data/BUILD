load("@rules_java//java:defs.bzl", "java_library")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_library(
  name = "data",
  visibility = ["//visibility:public"],
  srcs = glob(
    ["*.java"],
    exclude = ["ChappieDataModule.java"],
  ),
  deps = [
    "//profiler/chappie/util",
    "//profiler/eflect/data",
    "//profiler/eflect/util",
  ],
)

java_library(
  name = "module",
  visibility = ["//visibility:public"],
  srcs = ["ChappieDataModule.java"],
  deps = [
    ":data",
    "//profiler/eflect/util",
    "@clerk//clerk/inject:component",
    ":dagger",
  ],
)
