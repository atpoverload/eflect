# eflect benchmark suite

This document describes some high-level designs for a macrobenchmark suite that uses [`eflect`]() to analyze Java runtimes. This work may be an extension of the [`jmh`](https://openjdk.java.net/projects/code-tools/jmh) to enforce a hermetic environment. We want to support as many flavors of Java as possible and potentially begin exploring language-agnostic profiling.

## profiling methods

This framework would use [`eflect`]() to enforce energy-awareness in any Java application. The benchmark suite can mimic other testing frameworks to allow integration into commonly used toolkits. The end goal would be a testing framework that could be used anonymously. This is the primary reason I champion [`dagger`] because it can provide us with that hook.

We can also explore implementations to improve performance and/or provide external profiling by only monitoring the OS. Here are the potential goals that could be worked towards:

 - centralized energy-awareness; potential integration into `/proc/[pid]/stat` as a module
 - extend `rapl` and `eflect` support to other architectures; DI is powerful here because it can remove bloat from enum-based approaches with compile-time correctness.
 - runtime decoupling of `eflect` and the application
 - language-agnostic data containers that are optimized for exchange (not the CSVs I have been using)

I will admit that some of the work involved in the external implementation tasks feel moderately above my current skill level. Some of them are almost certainly too ambitious as well.

## Pure Java API

I am going to give a high-level picture of what the Java benchmarking API could look like. First, we would like to use Java's annotations to link together the stages of a benchmark; the reason to do this is to guarantee compile-time correctness and removal of potential performance loss from calling the benchmark method (same as a unit test). An example of an energy footprint evaluation system could look like:

```java
private final double error = 5.0;

@Param({"25", "50", "75"})
public int n;

@Param({"25.0", "30.0", "40.0"})
public double SLA;

@Benchmark
public void workload(int n, double SLA) {
  assertThat(fibonacci(n)).isWithin(SLA, error);
}
```

This would assert that the computation of the n-th fibonacci number's implementation is within 5J of a group of SLAs. This could be extended to deal with different kinds of data or even to integrate optimizations:

```java
@Benchmark(profile = {MethodRanking.class})
public void workload(int n, MethodRanking ranking) {
  assertThat(fibonacci(n)).isWithin(ranking, error);
}

@Benchmark(profile = {EnergyFootprint.class})
public void workload(int n, Knob[] knobs) {
  optimize(fibonacci(n)).with(knobs);
}
```

The `jmh` also has options to fork the runtime, which would create a fresh one. This could potentially resolve some of the issues we saw previously with mismatched method rankings. I have a lot more reading and testing to do before I can have a better understanding of how to do such a thing, let alone if it is even truly possible.

This work would be able to support library analysis because we would be creating a reliable environment to evaluate provided applications instead of tuning the benchmark itself.

These are very rough examples and again may be beyond the scope of what we can realistically do.

## External profiling

As we have discussed, the current `eflect` algorithm is language-agnostic because it uses pure OS information. This means I could wrote a command-line tool like:

```bash
> eflect $command # watch [pid1] until it terminates
  REPORT FOR PROCESS 25403 (fooCommand):
     - observed from  [timestamp1] to [timestamp2]
     - 50J / 2000J consumed
     - 1.21 ± 0.15J over runtime
     - top consuming methods:
        + FooServer.fetchFoo   : (37%)
        + FooServer.discardFoo : (12%)
        + FooServer.fooToBar   : (7%)
        ...
> eflect --pid=$pid1 # watch [pid1] until it terminates
  REPORT FOR PROCESS 25403 (fooClient):
     - observed from  [timestamp1] to [timestamp2]   
      ...
> eflect --pid=$pid2 --time 30s # watch [pid2] for 30s
  REPORT FOR PROCESS 25531 (fooServer):
    - observed from  [timestamp1] to [timestamp2]   
      ...
> eflect --pid=$pid3 --output=log.txt # watch [pid2] for 30s
 WROTE REPORT FOR PROCESS 243 (otherFooClient) to [path]/log.txt
```

This does not provide direct awareness, so the program still needs a signal from `eflect`. This does have the advantage of potentially being a lightweight tool that should be usable on any linux OS.
