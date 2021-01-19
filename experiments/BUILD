load("@rules_java//java:defs.bzl", "java_library", "java_binary")

java_library(
  name = "monitor",
  visibility = ["//visibility:public"],
  srcs = [
    "EflectCalmnessMonitor.java",
  ],
  deps = [
    "@eflect//java/eflect:freq",
    "@eflect//java/eflect:linux_eflect",
    "@eflect//java/eflect/util",
  ]
)

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = [
    "DaCapo.java",
  ],
  deps = [
    ":monitor",
    "@dacapo",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/data/dacapo"],
  args = ["-c eflect.experiments.DaCapo --no-validation"]
)

java_binary(
  name = "label_image",
  visibility = ["//visibility:public"],
  main_class = "eflect.experiments.LabelImage",
  srcs = [
    "LabelImage.java",
  ],
  deps = [
    ":monitor",
    "@org_tensorflow_tensorflow",
    "@org_tensorflow_libtensorflow",
  ],
  jvm_flags = ["-Deflect.output=/home/timur/projects/data/tensorflow"]
)
