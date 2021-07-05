#!/bin/bash

BENCHMARK=$1
SIZE=$2
RUNS=$3
SCRATCH_ROOT=scratch

pids=""
for i in `seq 1 1 $RUNS`; do
  scripts/tensorflow.sh
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done


ARGS=$@

JVM_OPTS=""
if [ ! -z $DATA_DIR ]; then
  JVM_OPTS+="--jvmopt=\"-Deflect.output.directory=$DATA_DIR\""
fi
if [ ! -z $PERIOD ]; then
  JVM_OPTS+=" --jvmopt=\"-Deflect.period.default=$PERIOD\""
fi

MODEL_DIR=$PWD/resources/inception_graph_v3/inception_v3_2016_08_28_frozen.pb
DATA=$PWD/resources/test.jpeg

bazel run java/eflect/experiments:tensorflow $JVM_OPTS -- $MODEL_DIR $DATA
