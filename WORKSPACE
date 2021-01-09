workspace(name = "eflect")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# data collection back-end
git_repository(
    name = "clerk",
    commit = "f2387bfd9f6bb56d2623e68db4c9ae4eb263d53c",
    shallow_since = "1609783280 -0700",
    remote = "https://github.com/timurbey/clerk.git",
)
load("@clerk//:clerk_deps.bzl", "clerk_deps")
clerk_deps()

load("eflect_deps.bzl", "eflect_data_deps")
eflect_data_deps()

# TODO(timurbey): get the build rule to directly build instead of the genrule
new_local_repository(
    name = "async_profiler",
    path = "./async-profiler",
    build_file_content = """
load("@rules_java//java:defs.bzl", "java_import")

genrule(
  name = "async-profiler-lib",
  visibility = ["//visibility:public"],
  cmd = "cp ./async-profiler/build/libasyncProfiler.so $@",
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

# injection deps (dagger)
# load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")
# maven_install(
#     artifacts = DAGGER_ARTIFACTS,
#     repositories = DAGGER_REPOSITORIES,
# )
