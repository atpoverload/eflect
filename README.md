# `eflect`

`eflect` is an energy accounting system for Java applications. `eflect` uses cross-layer, periodic sampling to produce application-granular energy data. `eflect` currently supports Intel-Linux systems. Currently we are working towards support Android as well.

## profiling an application with `eflect`

This section goes over a couple different use-cases where `eflect` can be used to perform energy profiling of a user application.

### external cli profiling

The `eflect` clients allow for a user to integrate 

a focus on running `eflect` as a standalone server that can handle multiple tenants.

## Data

`eflect` produces `EnergyFootprint`s of application threads:

```json
{
  "id": 1,
  "name": "Thread-1",
  "energy": 100,
  "start": 0,
  "end": 1000,
  "stack_traces": ["trace1", "trace2"]
}
```
