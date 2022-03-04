load("@rules_python//python:defs.bzl", "py_binary", "py_library")
load("@eflect_client_py_deps//:requirements.bzl", "requirement")

py_binary(
  name = "processing",
  srcs = ["processing.py"],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sample:sample_py_protos",
    requirement("numpy"),
    requirement("pandas"),
  ]
)

py_binary(
  name = "client",
  main = "client.py",
  srcs = ["client.py"],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sampler:sampler_py_grpc",
  ]
)

py_binary(
  name = "harness",
  main = "harness.py",
  srcs = [
    "harness.py",
    "processing.py"
  ],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sampler:sampler_py_grpc",
    ":client",
  ]
)
