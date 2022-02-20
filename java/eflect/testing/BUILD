load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "fake_eflect",
  visibility = ["//visibility:public"],
  srcs = ["FakeEflect.java"],
  deps = [
    "//java/eflect/sample",
    "//java/eflect/sample:jiffies",
  ],
  exports = [
    "//java/eflect/sample",
    "//java/eflect/sample:jiffies",
  ]
)

java_binary(
  name = "fake_run",
  visibility = ["//visibility:public"],
  main_class = "eflect.testing.FakeEflect",
  runtime_deps = [
    ":fake_eflect",
    "@com_google_protobuf//:protobuf_java",
  ]
)
