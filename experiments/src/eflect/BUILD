load("@rules_java//java:defs.bzl", "java_binary")
load("@dagger//:workspace_defs.bzl", "dagger_rules")

dagger_rules()

java_binary(
  name = "eflect",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "//profiler/eflect",
    "@dacapo",
    "@clerk//clerk",
    "@clerk//clerk/util:logger",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/sandbox/eflect/data"],
)

java_binary(
  name = "chappie",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "//profiler/chappie",
    "@dacapo",
    "@clerk//clerk",
    "@clerk//clerk/util:logger",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/sandbox/eflect/data"],
)
