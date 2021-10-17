load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")

proto_library(
  name = "sample_protos",
  visibility = ["//visibility:public"],
  srcs = glob(["*.proto"])
)

java_proto_library(
  name = "sample_java_protos",
  visibility = ["//visibility:public"],
  deps = [":sample_protos"]
)
