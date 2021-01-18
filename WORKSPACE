workspace(name = "eflect")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# data collection back-end
git_repository(
    name = "clerk",
    commit = "6f67b0060a19c03902e5243fe3c008935334c331",
    shallow_since = "1610998074 -0700",
    remote = "https://github.com/timurbey/clerk.git",
)
load("@clerk//:clerk_deps.bzl", "clerk_deps")
clerk_deps()

load("eflect_deps.bzl", "eflect_data_deps", "eflect_experiment_deps")
eflect_data_deps()
eflect_experiment_deps()
