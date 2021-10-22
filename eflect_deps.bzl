load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def eflect_deps():
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
    # dep for linux systems
    if not native.existing_rule("jRAPL"):
      git_repository(
          name = "jRAPL",
          commit = "7ab72298aada3f9f282fba3011c90671ed54e64b",
          shallow_since = "1625448924 -0600",
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
