load("@rules_python//python:defs.bzl", "py_binary")

py_binary(
  name = "to_footprint",
  srcs = ["to_footprint.py"],
  visibility = ["//visibility:public"],
  deps = [
    "@py-eflect//eflect/processing",
    "@py-eflect//protos/sample:sample_py_protos",
  ]
)
