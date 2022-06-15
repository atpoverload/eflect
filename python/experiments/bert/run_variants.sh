#!/bin/bash

# runs all variants of bert that are available in the $PWD/models directory
DATA_DIR="${PWD}/data"
rm -r "${DATA_DIR}"
mkdir "${DATA_DIR}"

DATA_SET=CoLA

for VARIANT in models/*; do
  bash ./run_variant.sh "${VARIANT##*/}" "${DATA_SET}"
done

bazel run //python/eflect:processing -- $PWD/data/**/*.pb
