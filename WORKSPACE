workspace(name = "eflect")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

load("eflect_deps.bzl", "eflect_deps")
eflect_deps()

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")
rules_java_dependencies()
rules_java_toolchains()

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()
