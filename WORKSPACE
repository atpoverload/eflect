load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# dagger deps
RULES_JVM_EXTERNAL_TAG = "3.3"
RULES_JVM_EXTERNAL_SHA = "d85951a92c0908c80bd8551002d66cb23c3434409c814179c0ff026b53544dab"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

DAGGER_TAG = "2.28.1"
DAGGER_SHA = "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd"

http_archive(
    name = "dagger",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    sha256 = DAGGER_SHA,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

maven_install(
    artifacts = DAGGER_ARTIFACTS,
    repositories = DAGGER_REPOSITORIES,
)

load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_jar")

maven_jar(
    name = "net_java_dev_jna_jna",
    artifact = "net.java.dev.jna:jna:5.4.0",
    repository = "https://repo1.maven.org/maven2",
)

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "clerk",
    commit = "82e8ac0c39add7f519caaa1f884912ccaa933016",
    shallow_since = "1600974126 -0600",
    remote = "https://github.com/timurbey/clerk.git",
)

# TODO(timurbey): update when we have a working draft of jrapl
git_repository(
    name = "jRAPL",
    commit = "6c3c08f796bf21ed4668d0cb690ce1732d634181",
    shallow_since = "1600653593 -0600",
    remote = "https://github.com/timurbey/jRAPL.git",
)

# TODO(timurbey): get the build rule to directly build instead of the genrule
new_local_repository(
    name = "async_profiler",
    path = "/home/timur/sandbox/async-profiler",
    build_file_content = """
load("@rules_java//java:defs.bzl", "java_import")

genrule(
  name = "async-profiler-lib",
  visibility = ["//visibility:public"],
  cmd = "cp /home/timur/sandbox/async-profiler/build/libasyncProfiler.so $@",
  outs = ["libasyncProfiler.so"],
)

java_import(
    name = "async_profiler",
    visibility = ["//visibility:public"],
    jars = [
      "build/async-profiler.jar"
    ],
)
"""
)

http_archive(
    name = "dacapo",
    urls = ["https://clerk-deps.s3.amazonaws.com/dacapo.zip"],
)

# jmh deps
http_archive(
  name = "rules_jmh",
  strip_prefix = "buchgr-rules_jmh-6ccf8d7",
  url = "https://github.com/buchgr/rules_jmh/zipball/6ccf8d7b270083982e5c143935704b9f3f18b256",
  type = "zip",
  sha256 = "dbb7d7e5ec6e932eddd41b910691231ffd7b428dff1ef9a24e4a9a59c1a1762d",
)

load("@rules_jmh//:deps.bzl", "rules_jmh_deps")
rules_jmh_deps()
load("@rules_jmh//:defs.bzl", "rules_jmh_maven_deps")
rules_jmh_maven_deps()
