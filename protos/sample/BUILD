load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
# TODO(timur): what is the right rule?
load("@com_google_protobuf//:protobuf.bzl", "py_proto_library")

# sample protos
SAMPLE_SOURCES = [
  # place all data sources here and keep them sorted
  "jiffies.proto",
  "rapl.proto",
  # don't modify under this
  "sample.proto"
]

proto_library(
  name = "sample_protos",
  visibility = ["//visibility:public"],
  srcs = SAMPLE_SOURCES,
)

java_proto_library(
  name = "sample_java_protos",
  visibility = ["//visibility:public"],
  deps = [":sample_protos"],
)

py_proto_library(
  name = "sample_py_protos",
  visibility = ["//visibility:public"],
  srcs = SAMPLE_SOURCES,
)
