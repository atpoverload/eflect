load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@rules_jvm_external//:defs.bzl", "maven_install")

def eflect_data_deps():
    """Loads the dependencies for eflect's data sources."""
    if not native.existing_rule("jRAPL"):
      git_repository(
          name = "jRAPL",
          commit = "6c3c08f796bf21ed4668d0cb690ce1732d634181",
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
      http_archive(
          name = "async-profiler",
          urls = ["https://clerk-deps.s3.amazonaws.com/async-profiler.zip"],
      )
