load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# data collection back-end
git_repository(
    name = "clerk",
    commit = "f534c94c081f68e4117a36d1f6a54d15f9ab3ccb",
    shallow_since = "1606411792 -0700",
    remote = "https://github.com/timurbey/clerk.git",
)

load("@clerk//:clerk_deps.bzl", "clerk_deps")
clerk_deps()

# data sources
git_repository(
    name = "jRAPL",
    commit = "6c3c08f796bf21ed4668d0cb690ce1732d634181",
    shallow_since = "1600653593 -0600",
    remote = "https://github.com/timurbey/jRAPL.git",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
  name = "net_java_dev_jna_jna",
  artifacts = ["net.java.dev.jna:jna:5.4.0"],
  repositories = ["https://repo1.maven.org/maven2"],
)

# injection deps (dagger)
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")
maven_install(
    artifacts = DAGGER_ARTIFACTS,
    repositories = DAGGER_REPOSITORIES,
)

# benchmarking deps
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "dacapo",
    urls = ["https://clerk-deps.s3.amazonaws.com/dacapo.zip"],
)

load("@rules_jmh//:deps.bzl", "rules_jmh_deps")
rules_jmh_deps()

load("@rules_jmh//:defs.bzl", "rules_jmh_maven_deps")
rules_jmh_maven_deps()
