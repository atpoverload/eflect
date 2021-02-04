load("@rules_java//java:defs.bzl", "java_binary", "java_library")

genrule(
  name = "lib-async-profiler",
  visibility = ["//visibility:public"],
  srcs = ["@async-profiler//:lib-async-profiler"],
  outs = ["libasyncProfiler.so"],
  cmd = "cp -f $< $@",
)

java_library(
  name = "testing",
  visibility = ["//visibility:public"],
  srcs = ["FakeLinuxEflect.java"],
  deps = [
    "//java/eflect",
    "//java/eflect/data/async",
    "//java/eflect/util",
    "@async-profiler",
  ],
  exports = [
    "//java/eflect",
    "//java/eflect/data",
  ],
  data = [
    ":lib-async-profiler",
  ]
)

java_binary(
  name = "driver",
  visibility = ["//visibility:private"],
  main_class = "eflect.testing.FakeLinuxEflect",
  runtime_deps = [
    ":testing",
  ],
  data = [

  ]
)
