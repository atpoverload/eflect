load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "@eflect//java/eflect",
    "@eflect//java/eflect/util",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/eflect"],
  args = ["-c eflect.experiments.DaCapo"]
)
