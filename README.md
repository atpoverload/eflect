# `eflect`

`eflect` is an energy accounting system for Java applications. `eflect` uses cross-layer, periodic sampling to produce application-granular energy data. `eflect` currently supports systems that use `/proc` and the `msr` module.

## Data

`eflect` produces `EnergyFootprint`s of application threads:

```
{
  "id": 1,
  "name": "Thread-1",
  "energy": 100,
  "start": 0,
  "end": 1000,
  "stack_trace": ["trace1", "trace2", ...]
}
```

The output data can be written as a `.csv`, where the energy is divided evenly across all stack traces.

## Integration

You can integrate `eflect` into your application directly:

```java
Eflect eflect = new Eflect();
eflect.start();
myProgram.run();
eflect.stop();
System.out.println(eflect.read());
```
