load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DummyDaCapo.java",
  ],
  deps = [
    "@eflect//java/eflect/testing",
    "@eflect//java/eflect/util",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/mnt/c/Users/Timur/Documents/projects/eflect"],
  args = ["-c eflect.experiments.DummyDaCapo"]
)
