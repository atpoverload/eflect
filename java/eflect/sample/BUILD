load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "jiffies",
  srcs = ["JiffiesDataSources.java"],
  visibility = ["//visibility:public"],
  deps = [
    "//java/eflect/util:logger",
    "//protos/sample:sample_java_protos",
  ],
  exports = [
    "//protos/sample:sample_java_protos",
  ]
)

java_library(
  name = "rapl",
  visibility = ["//visibility:public"],
  srcs = ["RaplDataSources.java"],
  deps = [
    "//java/eflect/sample",
    "@jRAPL//src/java/jrapl",
  ],
  data = [
    ":lib-cpu-scaler",
  ]
)

java_library(
  name = "sample",
  visibility = ["//visibility:public"],
  srcs = glob(["SampleCollector.java"]),
  deps = [
    "//protos/sample:sample_java_protos",
    "@clerk//java/clerk",
    "@clerk//java/clerk/storage",
    "@clerk//java/clerk/util",
  ],
  exports = [
    "//protos/sample:sample_java_protos",
  ]
)

genrule(
  name = "lib-cpu-scaler",
  visibility = ["//visibility:private"],
  srcs = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  outs = ["libCPUScaler.so"],
  cmd = "cp -f $< $@",
)
