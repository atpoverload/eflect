load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def eflect_deps():
    """Loads the dependencies for eflect's data sources."""
    if not native.existing_rule("clerk"):
      git_repository(
          name = "clerk",
          commit = "ef9ebdf459eebb753cb8d77e144f3cc554a18332",
          shallow_since = "1621661263 -0600",
          remote = "https://github.com/timurbey/clerk.git",
      )
    if not native.existing_rule("jRAPL"):
      git_repository(
          name = "jRAPL",
          commit = "a09480ce128658d2916a9e52e02d23988dcaee2b",
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
