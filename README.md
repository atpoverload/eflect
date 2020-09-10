# eflect benchmark suite

This document describes an API for a macrobenchmark suite that uses a customized energy profiler to analyze Java runtimes. This work may be built on top of the [`jmh`](https://openjdk.java.net/projects/code-tools/jmh) to enforce a hermetic environment independently of the system environment. We intend to primarily support Java (which versions? it's easier to implement in 8+) and Android systems with our bootstrapper.

## in-situ vs external profiling

Our framework is designed around in-situ profiler called [`eflect`]() so all data are accessible within the same runtime. External profiling can also be performed but requires additional user effort to align with `eflect` data.

## Pure Java API

This section describes the intended API for this benchmark suite. This material is subject to change. Our framework automates the execution and management of the `eflect` profiler to capture precise representations of the runtime. Annotations can be used to customize the profiler (I'd rather do this through dagger honestly). We can provide the following profiles:

 - total energy
 - timestamped energy trace
 - method energy ranking

Profile evaluation is also customizable through algebraic profile methods.

Below is an example benchmark for evaluating method rankings of `something`:

```java
@Setup
public void setUp() {
  // perform something, might need to prevent the jmh from optimizing?
}

@Benchmark
public void workload1() {
  // program
}

@Benchmark
public void workload2() {
  // program
}

@Evaluation
public void evaluateRankings(MethodRanking ... rankings) {
  double totalScore = 0;
  double totalError = 0;
  for (int i = 0; i < rankings.length; i++) {
    // performs pearson correlation between the energy values for each workload versus the baseline?
    double score = baselineRanking.compare(rankings[i]);
    totalScore += score;
    totalError += (1 - score ^ 2) / (rankings[i].getCount() - 1);
  }
  logger.info("score of " + totalScore + "\u00B1" + sqrt(totalError));
}
```

Collected profiles can also be written to disk. We intend to support `csv`, `protobuf`, `sql`, etc.

## External profiling

To profile an arbitrary program with some `[pid]`, you can use the eflect command-line:

```bash
## this is the implementation we want ##
# eflect --pid [pid]
java -jar eflect-profiler.jar --pid [pid]
```

all collected profiles will be dumped to the provided location.

<!-- I thought about this and here's what I propose for library analysis on machine learning:

We should run each experiment, which would encompass the entire pipeline, on a workstation, an android emulator, and a physical device. There are two goals:
Compare the runtime behavior/rankings. Comparing the same algorithm on multiple unique environments exposes information about the implementation differences, which is a goal for optimization. I know for a fact that the android implementations do not have all the Java APIs available in non-mobile systems. This is not a trivial difference and almost certainly requires a different optimization strategy.
Develop some sort of "energy benchmark test". Most machine learning and blockchain algorithms have some bounds on energy consumption. We provide a benchmark framework such that users define the benchmark (similar to unit testing frameworks).
The first two of these are easy (we've already done them). The third is not easy; thinking about it, it is actually quite large in magnitude.

Let's say I implement a random forest. It's easy enough to write an automation suite that profiles it and provides a summary (already done). What is not easy is the evaluation of the run. Most studies of ML focus on a specific model (like the paper you shared). They frequently present that runtime and energy are correlated, so you can compute an energy complexity based on input size. This may be true when the model is totally isolated but that is not a real environment. Therefore, we should support the ability to run not just a model but an entire pipeline.

Let's now add another piece to our pipeline, maybe an optimizer that uses boosting to mine the data further. Our system should be able to accomplish the following: first, individual units are profiled to get a footprint. Second, we test larger topologies. In the example I provided, the model has two pieces: the forest predictor and the boosting algorithm. We want to know how these pieces scale together and compare them to the individual cases. The goal is to produce a hermetic environment for energy evaluation.of programs.

There are a number of reasons I think this is a strong direction:

1) user-driven: all of the details can be provided by a user. All we do is automate the profiling and evaluation phases.

2) scalability: we should be integratable with any environment. Being able to achieve this means that any system can be profiled regardless of implementation.

3) flexibility: test frameworks tend to be user-friendly and act as a bootstrapper for a workload.

We have also discussed energy debugging. This goes hand-in-hand with a test framework. The evaluation is just a user choice. I exercise my workload and the evaluation strategy can use finer grain techniques as needed to isolate behavior. This system I've described should be completely customizable (even the profiler if a user needs something more than what we provide).

There is the additional challenge of our system being solely on Java. As we discussed, eflect's jiffies reconstruction is language-agnostic and is only a detail of a linux implementation. Therefore, if you have any energy data, you can produce a footprint externally. However this doesn't allow for method ranking which, at least to me, seems critical to a useful test framework for this kind of system. Perhaps there's no reason to worry about this because it might require a workforce beyond the three of us. -->
