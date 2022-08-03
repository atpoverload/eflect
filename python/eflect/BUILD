load("@rules_python//python:defs.bzl", "py_binary", "py_test")
load("@eflect_client_py_deps//:requirements.bzl", "requirement")

py_binary(
  name = "virtualization",
  srcs = ["virtualization.py"],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sample:sample_py_protos",
    requirement("numpy"),
    requirement("pandas"),
  ]
)

py_test(
  name = "processing_test",
  srcs = ["processing_test.py", "collector.py"],
  data = glob(["resources/*.textproto"]),
  deps = [
    ":processing",
    "//protos/sample:sample_py_protos",
    requirement("numpy"),
    requirement("pandas"),
    requirement("protobuf"),
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
    "virtualization.py"
  ],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sampler:sampler_py_grpc",
    ":client",
  ]
)
