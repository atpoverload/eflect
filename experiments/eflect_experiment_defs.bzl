def eflect_jmh_benchmark(name, profiler_class, srcs, deps=[], tags=[], plugins=[], **kwargs):
    """Runs JMH benchmarks from EflectProfiler.
    Implementation dervied from https://github.com/buchgr/rules_jmh.
    """
    plugin_name = "_{}_jmh_annotation_processor".format(name)
    native.java_plugin(
        name = plugin_name,
        visibility = ["//visibility:private"],
        deps = ["@org_openjdk_jmh_jmh_generator_annprocess"],
        processor_class = "org.openjdk.jmh.generators.BenchmarkProcessor",
        tags = tags,
    )
    native.java_binary(
        name = name,
        srcs = srcs,
        main_class = profiler_class,
        deps = deps + [
          "@org_openjdk_jmh_jmh_core"
        ],
        plugins = plugins + [plugin_name],
        tags = tags,
        **kwargs
    )
