load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "eflect",
  visibility = ["//visibility:public"],
  srcs = ["Eflect.java"],
  deps = [
    "//java/eflect/sample:jiffies",
    "//java/eflect/sample:rapl",
    "//java/eflect/util",
    "//java/eflect/util:logger",
    "//protos/sample:sample_java_protos",
  ],
  exports = [
    "//java/eflect/sample:jiffies",
    "//java/eflect/sample:rapl",
    "//java/eflect/util",
    "//java/eflect/util:logger",
    "//protos/sample:sample_java_protos",
  ]
)

java_binary(
  name = "dry_run",
  main_class = "eflect.Eflect",
  visibility = ["//visibility:public"],
  runtime_deps = [
    ":eflect",
  ]
)
