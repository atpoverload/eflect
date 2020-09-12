load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

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
    commit = "d40ff04f076cbe094510eb4c1c81b53d49045e3a",
    shallow_since = "1599832025 -0600",
    remote = "https://github.com/timurbey/clerk.git",
)

git_repository(
    name = "jRAPL",
    commit = "be6f46bccc52f5439947de1aba9bdca52401e471",
    shallow_since = "1598918413 -0400",
    remote = "https://github.com/pl-eflect/jRAPL.git",
)
