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
    "@eflect//java/eflect:freq",
    "@eflect//java/eflect/util",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/data"],
  args = ["-c eflect.experiments.DaCapo"]
)

java_binary(
  name = "label_image",
  visibility = ["//visibility:public"],
  main_class = "eflect.experiments.LabelImage",
  srcs = [
    "LabelImage.java",
  ],
  deps = [
    "@eflect//java/eflect",
    "@eflect//java/eflect:freq",
    "@eflect//java/eflect/util",
    "@tensorflow",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/data/"]
)
