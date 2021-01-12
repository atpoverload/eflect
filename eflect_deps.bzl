load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@rules_jvm_external//:defs.bzl", "maven_install")

def eflect_data_deps():
    """Loads the dependencies for eflect's data sources."""
    if not native.existing_rule("jRAPL"):
      git_repository(
          name = "jRAPL",
          commit = "0f00bcec68fb10a325b3cb3906a6b6ef412e093f",
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

def eflect_experiment_deps():
  """Loads the dependencies to run eflect's experiments."""
  if not native.existing_rule("dacapo"):
    http_archive(
        name = "dacapo",
        urls = ["https://clerk-deps.s3.amazonaws.com/dacapo.zip"],
    )
  if not native.existing_rule("org_openjdk_jmh_jmh_core"):
    maven_install(
        name = "org_openjdk_jmh_jmh_core",
        artifacts = [
          "org.openjdk.jmh:jmh-core:1.27",
        ],
        repositories = ["https://repo1.maven.org/maven2"],
    )
  if not native.existing_rule("org_openjdk_jmh_jmh_generator_annprocess"):
    maven_install(
        name = "org_openjdk_jmh_jmh_generator_annprocess",
        artifacts = [
          "org.openjdk.jmh:jmh-generator-annprocess:1.27"
        ],
        repositories = ["https://repo1.maven.org/maven2"],
    )
