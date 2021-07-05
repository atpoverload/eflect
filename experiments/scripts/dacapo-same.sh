#!/bin/bash

BENCHMARK=$1
SIZE=$2
RUNS=$3
SCRATCH_ROOT=scratch

# i did this stupidly
OUTPUT_DIR=$DATA_DIR
pids=""
for i in `seq 1 1 $RUNS`; do
  export DATA_DIR=$OUTPUT_DIR/$i
  scripts/dacapo.sh $BENCHMARK -s $SIZE -n 1 --scratch-directory $SCRATCH_ROOT/$i &
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done
