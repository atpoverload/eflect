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

java_library(
  name = "proc_stat",
  srcs = [
    "ProcStatDataSources.java",
  ],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sample:sample_java_protos",
  ],
)

java_library(
  name = "proc_task",
  srcs = [
    "ProcTaskDataSources.java",
  ],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sample:sample_java_protos",
  ],
)

java_library(
  name = "sample",
  visibility = ["//visibility:public"],
  srcs = glob(["SampleCollector.java"]),
  deps = [
    "//protos/sample:sample_java_protos",
  ],
  exports = [
    "//protos/sample:sample_java_protos",
  ],
)

genrule(
  name = "lib-cpu-scaler",
  visibility = ["//visibility:private"],
  srcs = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  outs = ["libCPUScaler.so"],
  cmd = "cp -f $< $@",
)
