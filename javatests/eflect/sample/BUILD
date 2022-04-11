load("@rules_java//java:defs.bzl", "java_test")

java_test(
  name = "JiffiesDataSourcesTest",
  visibility = ["//visibility:public"],
  srcs = ["JiffiesDataSourcesTest.java"],
  deps = [
    "//java/eflect/sample:jiffies",
    "//protos/sample:sample_java_protos"
  ],
)

java_test(
  name = "RaplDataSourcesTest",
  visibility = ["//visibility:public"],
  srcs = ["RaplDataSourcesTest.java"],
  deps = [
    "//java/eflect/sample:rapl",
    "//protos/sample:sample_java_protos"
  ],
)
