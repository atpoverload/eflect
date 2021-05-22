#!/bin/bash

ARGS=$@

JVM_OPTS=""
if [ ! -z $DATA_DIR ]; then
  JVM_OPTS+="--jvmopt=\"-Deflect.output.directory=$DATA_DIR\""
fi
if [ ! -z $PERIOD ]; then
  JVM_OPTS+=" --jvmopt=\"-Deflect.period.default=$PERIOD\""
fi

bazel run java/eflect/experiments:dacapo $JVM_OPTS -- $ARGS --no-validation -c eflect.experiments.EflectCallback
