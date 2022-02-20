load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def eflect_sample_deps():
  """Loads the dependencies for eflect's sample protos."""
  if not native.existing_rule("rules_java"):
    http_archive(
      name = "rules_java",
      sha256 = "ccf00372878d141f7d5568cedc4c42ad4811ba367ea3e26bc7c43445bbc52895",
      strip_prefix = "rules_java-d7bf804c8731edd232cb061cb2a9fe003a85d8ee",
      urls = [
          "https://mirror.bazel.build/github.com/bazelbuild/rules_java/archive/d7bf804c8731edd232cb061cb2a9fe003a85d8ee.tar.gz",
          "https://github.com/bazelbuild/rules_java/archive/d7bf804c8731edd232cb061cb2a9fe003a85d8ee.tar.gz",
      ],
    )
  if not native.existing_rule("rules_proto"):
    http_archive(
      name = "rules_proto",
      sha256 = "2490dca4f249b8a9a3ab07bd1ba6eca085aaf8e45a734af92aad0c42d9dc7aaf",
      strip_prefix = "rules_proto-218ffa7dfa5408492dc86c01ee637614f8695c45",
      urls = [
          "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
          "https://github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
      ],
    )
  if not native.existing_rule("rules_python"):
    http_archive(
      name = "rules_python",
      sha256 = "09a3c4791c61b62c2cbc5b2cbea4ccc32487b38c7a2cc8f87a794d7a659cc742",
      strip_prefix = "rules_python-740825b7f74930c62f44af95c9a4c1bd428d2c53",
      url = "https://github.com/bazelbuild/rules_python/archive/740825b7f74930c62f44af95c9a4c1bd428d2c53.zip",
    )
  if not native.existing_rule("com_google_protobuf"):
    http_archive(
      name = "com_google_protobuf",
      sha256 = "3bd7828aa5af4b13b99c191e8b1e884ebfa9ad371b0ce264605d347f135d2568",
      strip_prefix = "protobuf-3.19.4",
      urls = ["https://github.com/protocolbuffers/protobuf/archive/v3.19.4.tar.gz"],
    )

def eflect_grpc_deps():
  """Loads the dependencies for eflect's data sources."""
  if not native.existing_rule("rules_java"):
    http_archive(
      name = "rules_java",
      sha256 = "ccf00372878d141f7d5568cedc4c42ad4811ba367ea3e26bc7c43445bbc52895",
      strip_prefix = "rules_java-d7bf804c8731edd232cb061cb2a9fe003a85d8ee",
      urls = [
          "https://mirror.bazel.build/github.com/bazelbuild/rules_java/archive/d7bf804c8731edd232cb061cb2a9fe003a85d8ee.tar.gz",
          "https://github.com/bazelbuild/rules_java/archive/d7bf804c8731edd232cb061cb2a9fe003a85d8ee.tar.gz",
      ],
    )
  if not native.existing_rule("rules_proto"):
    http_archive(
      name = "rules_proto",
      sha256 = "2490dca4f249b8a9a3ab07bd1ba6eca085aaf8e45a734af92aad0c42d9dc7aaf",
      strip_prefix = "rules_proto-218ffa7dfa5408492dc86c01ee637614f8695c45",
      urls = [
          "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
          "https://github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
      ],
    )
  if not native.existing_rule("rules_python"):
    http_archive(
      name = "rules_python",
      sha256 = "09a3c4791c61b62c2cbc5b2cbea4ccc32487b38c7a2cc8f87a794d7a659cc742",
      strip_prefix = "rules_python-740825b7f74930c62f44af95c9a4c1bd428d2c53",
      url = "https://github.com/bazelbuild/rules_python/archive/740825b7f74930c62f44af95c9a4c1bd428d2c53.zip",
    )
  if not native.existing_rule("rules_proto_grpc"):
    http_archive(
      name = "rules_proto_grpc",
      sha256 = "507e38c8d95c7efa4f3b1c0595a8e8f139c885cb41a76cab7e20e4e67ae87731",
      strip_prefix = "rules_proto_grpc-4.1.1",
      urls = ["https://github.com/rules-proto-grpc/rules_proto_grpc/archive/4.1.1.tar.gz"],
    )

def eflect_java_deps():
    """Loads the dependencies for eflect's java data sources."""
    if not native.existing_rule("jRAPL"):
      git_repository(
          name = "jRAPL",
          commit = "0f00bcec68fb10a325b3cb3906a6b6ef412e093f",
          shallow_since = "1610385469 -0700",
          remote = "https://github.com/timurbey/jRAPL.git",
      )
    if not native.existing_rule("async-profiler"):
      native.new_local_repository(
          name = "async-profiler",
          path = "./third_party/async-profiler",
          build_file_content = """
load("@rules_java//java:defs.bzl", "java_import")

filegroup(
    name = "lib-async-profiler",
    srcs = ["build/libasyncProfiler.so"],
    visibility = ["//visibility:public"],
)

java_import(
    name = "async-profiler",
    visibility = ["//visibility:public"],
    jars = [
      "build/async-profiler.jar"
    ],
)
"""
      )
