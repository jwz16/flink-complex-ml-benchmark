#!/bin/bash

source ./script_variables.sh

if [[ $1 = '--rebuild' ]]; then
  # build model archiver files
  pushd $(pwd)/workflows/damaged_vehicle_license_plate_extraction
  echo 'building model archiver files...'
  ./build_model_archivers.sh
  ./build_workflow_archiver.sh
  echo 'build model archiver files all done'
  popd
fi

MODEL_MAR_FILES=()
N=${#MODEL_NAMES[@]}

for model_name in ${MODEL_NAMES[@]}
do
  MODEL_MAR_FILES+=(${model_name}.mar)
done

CMD_SERVE="torchserve --start --ncs --model-store=/tmp/model-store --workflow-store /tmp/wf-store/ --models ${MODEL_MAR_FILES[@]}"
# CMD_SERVE="torchserve --start --ncs --model-store=/tmp/model-store --workflow-store /tmp/wf-store/ --models mobilenet_v1.mar damage_vehicle_yolo5.mar yolos-small-finetuned-license-plate-detection.mar"

# serve model/workflow
container_exists=false
if (( $(docker ps -a -f "name=^/torchserve_wf$" | wc -l) == 2 )); then
  container_exists=true
fi

if [[ "$container_exists" = false ]];
then
  docker run --rm \
    --name torchserve_wf \
    --shm-size=8g \
    --ulimit memlock=-1 \
    --ulimit stack=67108864 \
    -p8080:8080 \
    -p8081:8081 \
    -p8082:8082 \
    -p7070:7070 \
    -p7071:7071 \
    -v $(pwd)/workflows/damaged_vehicle_license_plate_extraction/model-store:/tmp/model-store \
    -v $(pwd)/workflows/damaged_vehicle_license_plate_extraction/wf-store:/tmp/wf-store \
    bu-cs551/torchserve-complex-ml-benchmark:latest "${CMD_SERVE}"
else
  docker start -a torchserve_wf
fi