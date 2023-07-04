#!/bin/bash

source ./script_variables.sh

if [[ $1 = '--rebuild' ]]; then
  # build model archiver files
  echo 'building model archiver files...'
  ./build_model_archivers.sh
  echo 'build model archiver files all done'
fi

MODEL_MAR_FILES=()
N=${#MODEL_NAMES[@]}

for model_name in ${MODEL_NAMES[@]}
do
  MODEL_MAR_FILES+=(${model_name}.mar)
done

CMD_SERVE="torchserve --start --ncs --model-store=/tmp/model-store --models ${MODEL_MAR_FILES[@]} --ts-config /tmp/config.properties"

# serve model/workflow
container_exists=false
if (( $(docker ps -a -f "name=^/torchserve$" | wc -l) == 2 )); then
  container_exists=true
fi

if [[ "$container_exists" = false ]];
then
  docker run --rm -d \
    --name torchserve \
    --shm-size=16g \
    --ulimit memlock=-1 \
    --ulimit stack=67108864 \
    -p8080:8080 \
    -p8081:8081 \
    -p8082:8082 \
    -p7070:7070 \
    -p7071:7071 \
    -v $(pwd)/model-store:/tmp/model-store \
    -v $(pwd)/config.properties:/tmp/config.properties \
    bu-cs551/torchserve-complex-ml-benchmark:latest "${CMD_SERVE}"
else
  docker start torchserve
fi