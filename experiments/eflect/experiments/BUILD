load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    "//java-vanilla/eflect",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/mnt/c/Users/Timur/Documents/projects/eflect"],
  args = ['-c eflect.experiments.DaCapo']
)

java_binary(
  name = "dummy_dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DummyDaCapo.java",
  ],
  deps = [
    "//java-vanilla/eflect:dummy",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/mnt/c/Users/Timur/Documents/projects/eflect"],
  args = ['-c eflect.experiments.DummyDaCapo']
)
