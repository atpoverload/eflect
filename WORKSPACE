workspace(name = "eflect")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# data collection back-end
git_repository(
    name = "clerk",
    commit = "237ad83528225681be040abd6308bd7d01f07857",
    shallow_since = "1611327503 -0700",
    remote = "https://github.com/timurbey/clerk.git",
)
load("@clerk//:clerk_deps.bzl", "clerk_deps")
clerk_deps()

load("eflect_deps.bzl", "eflect_data_deps", "eflect_experiment_deps")
eflect_data_deps()
eflect_experiment_deps()
