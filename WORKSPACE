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

load("//:eflect_experiment_deps.bzl", "eflect_experiment_deps")
eflect_experiment_deps()
