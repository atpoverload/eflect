def eflect_benchmark(name, srcs, deps=[], **kwargs):
    """Runs JMH benchmarks from eflect.jmh.EflectProfiler.
    Implementation dervied from https://github.com/buchgr/rules_jmh.
    """
    plugin_name = "_{}_jmh_annotation_processor".format(name)
    native.java_plugin(
        name = plugin_name,
        deps = ["@org_openjdk_jmh_jmh_generator_annprocess"],
        processor_class = "org.openjdk.jmh.generators.BenchmarkProcessor",
        visibility = ["//visibility:private"],
    )
    native.java_binary(
        name = name,
        srcs = srcs,
        main_class = "eflect.jmh.EflectProfiler",
        deps = deps + [
          "//eflect/jmh",
        ],
        plugins = [plugin_name],
        **kwargs
    )
