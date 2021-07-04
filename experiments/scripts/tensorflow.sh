#!/bin/bash

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
