workspace(name = "eflect")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
git_repository(
    name = "py-eflect",
    commit = "0546909fbb01a4b3a830db933a3d639503bb4a32",
    shallow_since = "1625448924 -0600",
    remote = "https://github.com/timurbey/py-eflect.git",
)

load("@py-eflect//:eflect_deps.bzl", "eflect_proto_deps")
eflect_proto_deps()

load("eflect_deps.bzl", "eflect_deps")
eflect_deps()

#### GRPC DEPS
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
  name = "com_github_protocolbuffers_protobuf",
  sha256 = "cf754718b0aa945b00550ed7962ddc167167bd922b842199eeb6505e6f344852",
  strip_prefix = "protobuf-3.12.0",
  urls = [
    "https://mirror.bazel.build/github.com/protocolbuffers/protobuf/archive/v3.12.0.tar.gz",
    "https://github.com/protocolbuffers/protobuf/archive/v3.12.0.tar.gz",
  ],
)

http_archive(
    name = "io_grpc_grpc_java",
    # sha256 = "fa90de8a05f07111152e1ab45bf919ddbe9ad762b4b1dd89e4752f3c2ac16a1d",
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

# load("@com_google_protobuf//:protobuf_deps.bzl", "PROTOBUF_MAVEN_ARTIFACTS")
load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

maven_install(
    artifacts = [
        "com.google.api.grpc:grpc-google-cloud-pubsub-v1:0.1.24",
        "com.google.api.grpc:proto-google-cloud-pubsub-v1:0.1.24",
    ] + IO_GRPC_GRPC_JAVA_ARTIFACTS, # + PROTOBUF_MAVEN_ARTIFACTS,
    generate_compat_repositories = True,
    override_targets = IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS,
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
)

load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()
