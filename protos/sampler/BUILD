load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("@com_github_grpc_grpc//bazel:python_rules.bzl", "py_grpc_library", "py_proto_library")

# sampler protos
SAMPLER_SOURCES = [
  "sampler.proto",
]

proto_library(
  name = "sampler_protos",
  visibility = ["//visibility:public"],
  srcs = SAMPLER_SOURCES,
  deps = ["//protos/sample:sample_protos"],
)

java_proto_library(
  name = "sampler_java_protos",
  visibility = ["//visibility:public"],
  deps = [":sampler_protos"],
)

java_grpc_library(
  name = "sampler_java_grpc",
  visibility = ["//visibility:public"],
  srcs = [":sampler_protos"],
  deps = [":sampler_java_protos"],
)

py_proto_library(
  name = "sampler_py_protos",
  visibility = ["//visibility:public"],
  deps = [":sampler_protos"],
)

py_grpc_library(
  name = "sampler_py_grpc",
  visibility = ["//visibility:public"],
  srcs = [":sampler_protos"],
  deps = [":sampler_py_protos"],
)
