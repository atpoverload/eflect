#!/bin/bash

BERT_DIR="${PWD}/third_party/bert"
MODELS_DIR="${PWD}/models"
GLUE_DIR="${PWD}/glue_data"

MODEL="uncased_L-2_H-128_A-2"
TASK=CoLA

DATA_DIR="${PWD}/test-data"
rm -rf "${DATA_DIR}"
mkdir "${DATA_DIR}"

rm -rf /tmp/output/*
python3.8 "${BERT_DIR}/run_classifier.py" \
  --task_name="${TASK}" \
  --do_train=true \
  --do_eval=true \
  --data_dir="${GLUE_DIR}/${TASK}" \
  --vocab_file="${MODELS_DIR}/${MODEL}/vocab.txt" \
  --bert_config_file="${MODELS_DIR}/${MODEL}/bert_config.json" \
  --init_checkpoint="${MODELS_DIR}/${MODEL}/bert_model.ckpt" \
  --max_seq_length=128 \
  --train_batch_size=32 \
  --learning_rate=2e-5 \
  --num_train_epochs=3.0 \
  --output_dir=/tmp/output \
  --tracing_output_dir="${DATA_DIR}" &

BERT_PID=$!
../../../target/debug/client start --pid=${BERT_PID}
tail --pid=${BERT_PID} -f /dev/null
../../../target/debug/client stop
../../../target/debug/client read --output "${DATA_DIR}/eflect-data.pb"
