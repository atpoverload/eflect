load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@com_google_protobuf//:protobuf.bzl", "py_proto_library")
load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("@rules_proto_grpc//python:defs.bzl", "python_grpc_library")

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

# sampler protos
SAMPLER_SOURCES = [
  "sampler.proto",
]

proto_library(
  name = "sampler_protos",
  visibility = ["//visibility:public"],
  srcs = SAMPLER_SOURCES,
  deps = [":sample_protos"],
)

java_grpc_library(
  name = "sampler_java_grpc",
  visibility = ["//visibility:public"],
  protos = [
    ":sample_protos",
    ":sampler_protos",
  ],
)

python_grpc_library(
  name = "sampler_py_grpc",
  visibility = ["//visibility:public"],
  protos = [
    ":sample_protos",
    ":sampler_protos",
  ],
)
