load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
  name = "client",
  srcs = ["SamplerClient.java"],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sample:sample_java_protos",
    "//protos/sampler:sampler_java_protos",
    "//protos/sampler:sampler_java_grpc",
    "@io_grpc_grpc_java//api",
  ],
)

java_binary(
  name = "run_client",
  main_class = "eflect.sampler.SamplerClient",
  runtime_deps = [
    ":client",
    "@io_grpc_grpc_java//netty",
  ]
)

java_library(
  name = "server",
  srcs = ["SamplerServer.java"],
  visibility = ["//visibility:public"],
  deps = [
    "//java/eflect/sample",
    "//java/eflect/sample:jiffies",
    "//java/eflect/sample:rapl",
    "//java/eflect/util:logger",
    "//protos/sample:sample_java_protos",
    "//protos/sampler:sampler_java_protos",
    "//protos/sampler:sampler_java_grpc",
    "@io_grpc_grpc_java//api",
    "@io_grpc_grpc_java//stub",
  ],
)

java_binary(
  name = "run_server",
  main_class = "eflect.sampler.SamplerServer",
  runtime_deps = [
    ":server",
    "@io_grpc_grpc_java//netty",
  ]
)
