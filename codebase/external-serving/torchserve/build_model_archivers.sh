#!/bin/bash
source ./script_variables.sh

echo 'clean old model archiver files...'
rm -rf ./model-store/*.mar
echo 'done'

echo 'clean old docker containers'
docker rm -f mar 2>/dev/null
echo 'done'

# make sure the model-store is writable in docker container
mkdir -p $(pwd)/model-store
chown $(id -un):$(id -gn) $(pwd)/model-store

N=${#MODEL_NAMES[@]}
for (( i=0; i<N; i++ ));
do
  model_name=${MODEL_NAMES[$i]}
  handler_file=${HANDLER_FILES[$i]}
  
  echo -e "\nstart building, model name: ${model_name}, handler file: ${handler_file}"

  weights_name=pytorch_model.bin
  if [[ $model_name == yolo5_damage_vehicle ]]; then
    weights_name=pytorch_model.pt
  fi

  CONTAINER_MODEL_PATH=/home/model-server/${model_name}
  CONTAINER_SERIALIZED_FILE=$CONTAINER_MODEL_PATH/${weights_name}
  CONTAINER_CONFIG_JSON=$CONTAINER_MODEL_PATH/config.json
  CONTAINER_REPROCESSOR_CONFIG_JSON=$CONTAINER_MODEL_PATH/preprocessor_config.json
  CONTAINER_HANDLER=/home/model-server/${handler_file}

  HOST_HANDLER=$(pwd)/handlers/${handler_file}

  CMD_CREATE_MAR="torch-model-archiver --model-name ${model_name} --version 1.0 --serialized-file ${CONTAINER_SERIALIZED_FILE} --handler ${CONTAINER_HANDLER} --extra-files ${CONTAINER_CONFIG_JSON},${CONTAINER_REPROCESSOR_CONFIG_JSON} --export-path /home/model-server/model-store -f"

  if [[ $model_name == yolo5_damage_vehicle ]]; then
    CMD_CREATE_MAR="torch-model-archiver --model-name ${model_name} --version 1.0 --serialized-file ${CONTAINER_SERIALIZED_FILE} --handler ${CONTAINER_HANDLER} --export-path /home/model-server/model-store -f"
  fi

  if [[ $model_name == trocr_small_printed ]]; then
    CONTAINER_GENERATION_CONFIG_JSON=$CONTAINER_MODEL_PATH/generation_config.json
    CONTAINER_SPECIAL_TOKENS_MAP_JSON=$CONTAINER_MODEL_PATH/special_tokens_map.json
    CONTAINER_TOKENIZER_CONFIG_JSON=$CONTAINER_MODEL_PATH/tokenizer_config.json
    CONTAINER_SENTENCE_PIECE_BPE=$CONTAINER_MODEL_PATH/sentencepiece.bpe.model

    CMD_CREATE_MAR="torch-model-archiver -f --model-name ${model_name} --version 1.0 --serialized-file ${CONTAINER_SERIALIZED_FILE} --handler ${CONTAINER_HANDLER} --extra-files ${CONTAINER_CONFIG_JSON},${CONTAINER_REPROCESSOR_CONFIG_JSON},${CONTAINER_GENERATION_CONFIG_JSON},${CONTAINER_SPECIAL_TOKENS_MAP_JSON},${CONTAINER_TOKENIZER_CONFIG_JSON},${CONTAINER_SENTENCE_PIECE_BPE} --export-path /home/model-server/model-store"
  fi

  # build torch serve archiver
  docker run --rm --name mar -v $(pwd)/model-store:/home/model-server/model-store -v $(pwd)/models/${model_name}:${CONTAINER_MODEL_PATH} -v $HOST_HANDLER:$CONTAINER_HANDLER --entrypoint="" pytorch/torchserve:latest $CMD_CREATE_MAR

  echo -e "build success!"
  echo "model archiver file stored at: model-store/${model_name}.mar"
done
