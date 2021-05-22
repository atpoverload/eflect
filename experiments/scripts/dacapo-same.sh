#!/bin/bash

BENCHMARK=$1
SIZE=$2
RUNS=$3
SCRATCH_ROOT=scratch

pids=""
for i in `seq 1 1 $RUNS`; do
  scripts/dacapo.sh $BENCHMARK -s $SIZE -n 20 --scratch-directory $SCRATCH_ROOT/$i &
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done
