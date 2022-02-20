workspace(name = "eflect")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# deps for sample protos
load("eflect_deps.bzl", "eflect_sample_deps")
eflect_sample_deps()

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

# deps for python processing
load("@rules_python//python:pip.bzl", "pip_install")
pip_install(
   name = "eflect_client_py_deps",
   requirements = "//client:requirements.txt",
)

# deps for java collection
load("eflect_deps.bzl", "eflect_java_deps")
eflect_java_deps()

# deps for grpc
http_archive(
    name = "rules_proto_grpc",
    sha256 = "507e38c8d95c7efa4f3b1c0595a8e8f139c885cb41a76cab7e20e4e67ae87731",
    strip_prefix = "rules_proto_grpc-4.1.1",
    urls = ["https://github.com/rules-proto-grpc/rules_proto_grpc/archive/4.1.1.tar.gz"],
)

load("@rules_proto_grpc//:repositories.bzl", "rules_proto_grpc_toolchains", "rules_proto_grpc_repos")
rules_proto_grpc_toolchains()
rules_proto_grpc_repos()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", "grpc_deps")
grpc_deps()

# deps for python grpc
load("@rules_proto_grpc//python:repositories.bzl", rules_proto_grpc_python_repos = "python_repos")
rules_proto_grpc_python_repos()

# deps for java grpc
http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "60fde3372aea8986f909f00b685541b810da372fe895db66d1c99549e74a235a",
    strip_prefix = "grpc-java-1.33.0",
    url = "https://github.com/grpc/grpc-java/archive/refs/tags/v1.33.0.tar.gz",
)
http_archive(
    name = "rules_jvm_external",
    sha256 = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca",
    strip_prefix = "rules_jvm_external-4.2",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/4.2.zip",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_ARTIFACTS")
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS")
load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "PROTOBUF_MAVEN_ARTIFACTS")
load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

maven_install(
    artifacts = IO_GRPC_GRPC_JAVA_ARTIFACTS + PROTOBUF_MAVEN_ARTIFACTS,
    generate_compat_repositories = True,
    override_targets = IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS,
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
)

load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()

# http_archive(
#   name = "com_google_googleapis",
#   strip_prefix = "googleapis-7a961f3c98744ee1c27d1e190369a031b4119000",
#   urls = ["https://github.com/googleapis/googleapis/archive/7a961f3c98744ee1c27d1e190369a031b4119000.zip"],
#   sha256 = "9e49f4ab11b5c008bfe821ee7bb61aa55892cbcf6e2361d00a9cb412a918f388"
# )
# load("@com_google_googleapis//:repository_rules.bzl", "switched_rules_by_language")
# switched_rules_by_language(name = "com_google_googleapis_imports", grpc = True)
#
# load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
# git_repository(
#     name = "jRAPL",
#     commit = "7ab72298aada3f9f282fba3011c90671ed54e64b",
#     shallow_since = "1625448924 -0600",
#     remote = "https://github.com/timurbey/jRAPL.git",
# )
