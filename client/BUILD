load("@rules_java//java:defs.bzl", "java_binary")
load("@rules_python//python:defs.bzl", "py_binary", "py_library")
load("@eflect_py_deps//:requirements.bzl", "requirement")

py_binary(
  name = "processing",
  srcs = ["processing.py"],
  visibility = ["//visibility:public"],
  deps = [
    requirement("numpy"),
    requirement("pandas"),
  ]
)

java_binary(
  name = "java_client",
  main_class = "eflect.Client",
  srcs = ["Client.java"],
  deps = [
    "//protos/sample:sample_java_grpc",
    "//protos/sample:sample_java_protos",
    "@io_grpc_grpc_java//api",
  ]
)

py_binary(
  name = "py_client",
  main = "client.py",
  srcs = ["client.py"],
  visibility = ["//visibility:public"],
  deps = [
    "//protos/sample:sample_py_grpc",
    "//protos/sample:sample_py_protos",
    requirement("grpcio"),
  ]
)

py_binary(
  name = "py_harness",
  main = "harness.py",
  srcs = ["harness.py"],
  visibility = ["//visibility:public"],
  deps = [
    ":processing",
    "//protos/sample:sample_py_grpc",
    "//protos/sample:sample_py_protos",
    requirement("grpcio"),
  ]
)
