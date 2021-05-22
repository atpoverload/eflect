#!/bin/bash

run_same_experiment() {
  BENCHMARK=$1
  SIZE=$2
  for RUNS in `seq 2 1 5`; do
    export DATA_DIR=$OUTPUT_DIR/footprints/same/$RUNS/$BENCHMARK
    scripts/dacapo-same.sh $BENCHMARK $SIZE $RUNS
  done
  exit
}

OUTPUT_DIR=$1

SIZE=default
BENCHMARKS=(biojava jython xalan)

for BENCHMARK in ${BENCHMARKS[@]}; do
  run_same_experiment $BENCHMARK $SIZE
done

SIZE=large
BENCHMARKS=(avrora batik eclipse h2 pmd sunflow)

for BENCHMARK in ${BENCHMARKS[@]}; do
  run_same_experiment $BENCHMARK $SIZE
done

SIZE=huge
BENCHMARKS=(graphchi)

for BENCHMARK in ${BENCHMARKS[@]}; do
  run_same_experiment $BENCHMARK $SIZE
done
