load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@rules_jvm_external//:defs.bzl", "maven_install")

def eflect_data_deps():
    """Loads the dependencies for eflect's data sources."""
    if not native.existing_rule("jRAPL"):
      git_repository(
          name = "jRAPL",
          commit = "93a1cc95c699355753a184b714e083e9189d0aa9",
          shallow_since = "1603245714 -0400",
          remote = "https://github.com/timurbey/jRAPL.git",
      )
    if not native.existing_rule("net_java_dev_jna_jna"):
      maven_install(
          name = "net_java_dev_jna_jna",
          artifacts = ["net.java.dev.jna:jna:5.4.0"],
          repositories = ["https://repo1.maven.org/maven2"],
      )
    if not native.existing_rule("async-profiler"):
      native.new_local_repository(
          name = "async-profiler",
          path = "./async-profiler",
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
