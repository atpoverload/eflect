load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "sources",
  visibility = ["//visibility:public"],
  srcs = ["RaplDataSources.java"],
  deps = [
    "//java/eflect/data",
    "@jRAPL//src/java/jrapl",
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
