# `eflect`

`eflect` is an energy accounting system for Java applications. `eflect` uses cross-layer periodic sampling to produce application-granular energy data. `eflect` currently supports systems that use `/proc` and the `msr` module.

## Data

`eflect` produces `EnergyFootprint`s of application threads:

```
{
  "id": 1,
  "name": "Thread-1",
  "energy": 100,
  "start": 0,
  "end": 1000,
  "stack_trace": ["top_of_stack", "bottom_of_stack"]
}
```

The output data can be stored as a `.csv`, `.json`, or `.proto`, or it can be written to a sql database.

## Integration

You can integrate `eflect` into your application directly:

```java
Eflect eflect = Eflect.newProfiler();
eflect.start();
myProgram.run();
eflect.stop();
Collection<EnergyFootprint> eflect.read();
```

or with the `jmh`:

```java
@Benchmark
public void test1() {
  myProgram.run();
}

public static void main(String[] args) throws RunnerException {
  Options opt = new OptionsBuilder().addProfiler(EflectProfiler.class).build();
  new Runner(opt).run();
}
```

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
