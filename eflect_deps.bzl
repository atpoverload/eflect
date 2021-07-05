load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def eflect_deps():
    """Loads the dependencies for eflect's data sources."""
    if not native.existing_rule("clerk"):
      git_repository(
          name = "clerk",
          commit = "fca09da6425cbaf8a810924fefe8de7e8818551e",
          shallow_since = "1625416462 -0600",
          remote = "https://github.com/timurbey/clerk.git",
      )
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
