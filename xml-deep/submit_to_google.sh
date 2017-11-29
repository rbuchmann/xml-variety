#!/usr/bin/bash

BUCKET_NAME="s2s_model_test"
TRAINING_SET_NAME="xml_pairs"
JOB_NAME="xmlstm_train_$(date +%Y%m%d_%H%M%S)"
JOB_DIR="gs://$BUCKET_NAME/$JOB_NAME"
REGION="europe-west1"

gcloud ml-engine jobs submit training "$JOB_NAME" \
  --job-dir "$JOB_DIR" \
  --runtime-version 1.0 \
  --module-name trainer.xmlstm \
  --package-path ./trainer \
  --region "$REGION" \
  --config=trainer/cloudml-gpu.yaml \
  -- \
  --train-file "gs://$BUCKET_NAME/$TRAINING_SET_NAME"
