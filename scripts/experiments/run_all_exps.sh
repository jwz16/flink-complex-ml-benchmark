#!/bin/bash

source ./common_vars.sh

echo "remove previous experiments results"
rm -rf $EXPERIMENTS_RESULTS_DIR

# External serving frameworks experiments
./run_torchserve_exps.sh
# ./run_tfserving_exps.sh
# ./run_openvino_exps.sh

# Embedded serving frameworks experiments
# ./run_onnx_exps.sh
# ./run_tfsaved_exps.sh
# ./run_nd4j_exps.sh