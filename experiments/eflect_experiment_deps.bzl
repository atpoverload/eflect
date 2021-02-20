load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@rules_jvm_external//:defs.bzl", "maven_install")

def eflect_experiment_deps():
    """Loads the dependencies for eflect's data sources."""
    if not native.existing_rule("dacapo"):
      http_archive(
          name = "dacapo",
          urls = ["https://clerk-deps.s3.amazonaws.com/dacapo.zip"],
      )
    if not native.existing_rule("org_openjdk_jmh_jmh_core"):
      maven_install(
          name = "org_openjdk_jmh_jmh_core",
          artifacts = [
            "org.openjdk.jmh:jmh-core:1.27",
          ],
          repositories = ["https://repo1.maven.org/maven2"],
      )
    if not native.existing_rule("org_openjdk_jmh_jmh_generator_annprocess"):
      maven_install(
          name = "org_openjdk_jmh_jmh_generator_annprocess",
          artifacts = [
            "org.openjdk.jmh:jmh-generator-annprocess:1.27"
          ],
          repositories = ["https://repo1.maven.org/maven2"],
      )
