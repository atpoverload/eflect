load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "jiffies",
  srcs = ["JiffiesDataSources.java"],
  visibility = ["//visibility:public"],
  deps = [
    "//java/eflect/util:logger",
    "@py-eflect//protos/sample:sample_java_protos",
  ],
)

java_library(
  name = "rapl",
  visibility = ["//visibility:public"],
  srcs = ["RaplDataSources.java"],
  deps = [
    "@jRAPL//src/java/jrapl",
    "@py-eflect//protos/sample:sample_java_protos",
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
    "@py-eflect//protos/sample:sample_java_protos",
  ],
)

genrule(
  name = "lib-cpu-scaler",
  visibility = ["//visibility:private"],
  srcs = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  outs = ["libCPUScaler.so"],
  cmd = "cp -f $< $@",
)

java_library(
  name = "sampler",
  visibility = ["//visibility:public"],
  srcs = ["SamplerServer.java"],
  deps = [
    "//protos/sample:sample_java_protos",
    "//protos/sample:sample_java_grpc",
    "@io_grpc_grpc_java//api",
  ],
  exports = [
    "//protos/sample:sample_java_protos",
  ]
)
