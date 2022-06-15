#!/bin/bash

# runs the best-fit or "named" variants for bert
DATA_DIR="${PWD}/data"
rm -r "${DATA_DIR}"
mkdir "${DATA_DIR}"

VARIANTS=(
  "uncased_L-2_H-128_A-2" # tiny
  "uncased_L-4_H-256_A-4" # mini
  "uncased_L-4_H-512_A-8" # small
  "uncased_L-8_H-512_A-8" # medium
)
DATA_SET=CoLA

for VARIANT in "${VARIANTS[@]}"; do
  bash ./run_variant.sh "${VARIANT}" "${DATA_SET}"
done

bazel run //python/eflect:processing -- $PWD/data/**/*.pb
