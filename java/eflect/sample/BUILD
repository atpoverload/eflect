load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "jiffies",
  srcs = ["JiffiesDataSources.java"],
  visibility = ["//visibility:public"],
  deps = [
    "//java/eflect/util:logger",
    "//protos/sample:sample_java_protos",
  ],
)

java_library(
  name = "rapl",
  visibility = ["//visibility:public"],
  srcs = ["RaplDataSources.java"],
  deps = [
    "@jRAPL//src/java/jrapl",
    "//protos/sample:sample_java_protos",
  ],
  data = [
    ":lib-cpu-scaler",
  ]
)

genrule(
  name = "lib-cpu-scaler",
  visibility = ["//visibility:private"],
  srcs = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  outs = ["libCPUScaler.so"],
  cmd = "cp -f $< $@",
)
