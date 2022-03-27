#!/bin/bash

# downloads all models and data sets for bert
MODELS_DIR="models"
URL_STUB="https://storage.googleapis.com/bert_models/2020_02_20"
VARIANTS=(
  "uncased_L-2_H-128_A-2" # tiny
  "uncased_L-2_H-256_A-4"
  "uncased_L-2_H-512_A-8"
  "uncased_L-2_H-768_A-12"
  "uncased_L-4_H-128_A-2"
  "uncased_L-4_H-256_A-4" # mini
  "uncased_L-4_H-512_A-8" # small
  "uncased_L-4_H-768_A-12"
  "uncased_L-6_H-128_A-2"
  "uncased_L-6_H-256_A-4"
  "uncased_L-6_H-512_A-8"
  "uncased_L-6_H-768_A-12"
  "uncased_L-8_H-128_A-2"
  "uncased_L-8_H-256_A-4"
  "uncased_L-8_H-512_A-8" # medium
  "uncased_L-8_H-768_A-12"
  "uncased_L-10_H-128_A-2"
  "uncased_L-10_H-256_A-4"
  "uncased_L-10_H-512_A-8"
  "uncased_L-10_H-768_A-12"
  "uncased_L-12_H-128_A-2"
  "uncased_L-12_H-256_A-4"
  "uncased_L-12_H-512_A-8"
  "uncased_L-12_H-768_A-12" # base
)

rm -r "${MODELS_DIR}"
mkdir "${MODELS_DIR}"

for VARIANT in "${VARIANTS[@]}"; do
  wget "${URL_STUB}/${VARIANT}.zip"

  VARIANT_DIR="${MODELS_DIR}/${VARIANT}"
  mkdir "${VARIANT_DIR}"
  unzip "${VARIANT}.zip" -d "${VARIANT_DIR}"

  rm "${VARIANT}.zip"
done

# TODO(timur): i've restricted this to CoLA for now but it looks like we want
#   MRPC
python setup_glue_data.py --tasks CoLA --data_dir glue_data
