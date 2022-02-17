load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("@rules_proto_grpc//python:defs.bzl", "python_grpc_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@com_github_protocolbuffers_protobuf//:protobuf.bzl", "py_proto_library")

proto_library(
  name = "sample_protos",
  visibility = ["//visibility:public"],
  srcs = glob(["*.proto"]),
)

java_proto_library(
  name = "sample_java_protos",
  visibility = ["//visibility:public"],
  deps = [":sample_protos"],
)

py_proto_library(
  name = "sample_py_protos",
  visibility = ["//visibility:public"],
  srcs = glob(["*.proto"]),
)

java_grpc_library(
  name = "sample_java_grpc",
  visibility = ["//visibility:public"],
  protos = [":sample_protos"],
)

python_grpc_library(
  name = "sample_py_grpc",
  visibility = ["//visibility:public"],
  protos = [":sample_protos"],
)
