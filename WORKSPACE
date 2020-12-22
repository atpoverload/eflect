load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "clerk",
    commit = "f534c94c081f68e4117a36d1f6a54d15f9ab3ccb",
    shallow_since = "1606411792 -0700",
    remote = "https://github.com/timurbey/clerk.git",
)

load("@clerk//:clerk_deps.bzl", "clerk_deps")
clerk_deps()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

maven_install(
    artifacts = DAGGER_ARTIFACTS,
    repositories = DAGGER_REPOSITORIES,
)

maven_install(
  name = "net_java_dev_jna_jna",
  artifacts = ["net.java.dev.jna:jna:5.4.0"],
  repositories = ["https://repo1.maven.org/maven2"],
)

git_repository(
    name = "jRAPL",
    commit = "6c3c08f796bf21ed4668d0cb690ce1732d634181",
    shallow_since = "1600653593 -0600",
    remote = "https://github.com/timurbey/jRAPL.git",
)
