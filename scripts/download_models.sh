#!/bin/bash

MOBILENET_V1_URLS=(
  https://huggingface.co/google/mobilenet_v1_0.75_192/resolve/main/pytorch_model.bin 
  https://huggingface.co/google/mobilenet_v1_0.75_192/raw/main/preprocessor_config.json 
  https://huggingface.co/google/mobilenet_v1_0.75_192/raw/main/config.json
)

YOLO5_DAMAGE_VEHICLE_URLS=(
  https://huggingface.co/spaces/AI-Naga/Vehicle_Damage_Detection/resolve/main/Damage_Vehicle_Y5.pt
)

YOLOS_LICENSE_PLATE_DETECTION_URLS=(
  https://huggingface.co/nickmuchi/yolos-small-finetuned-license-plate-detection/resolve/main/pytorch_model.bin
  https://huggingface.co/nickmuchi/yolos-small-finetuned-license-plate-detection/raw/main/preprocessor_config.json  
  https://huggingface.co/nickmuchi/yolos-small-finetuned-license-plate-detection/raw/main/config.json
)

TROCR_SMALL_PRINTED=(
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/pytorch_model.bin
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/config.json
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/generation_config.json
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/preprocessor_config.json
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/sentencepiece.bpe.model
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/special_tokens_map.json
  https://huggingface.co/microsoft/trocr-small-printed/resolve/main/tokenizer_config.json
)

WEIGHTS_STORE_ROOT=../assets/weights/pytorch
mkdir -p $WEIGHTS_STORE_ROOT

for url in ${MOBILENET_V1_URLS[@]}; do
  curl -OL --create-dirs --output-dir $WEIGHTS_STORE_ROOT/mobilenet_v1 $url
done

for url in ${YOLO5_DAMAGE_VEHICLE_URLS[@]}; do
  curl -OL --create-dirs --output-dir $WEIGHTS_STORE_ROOT/yolo5_damage_vehicle $url
done
# rename weights file name
mv $WEIGHTS_STORE_ROOT/yolo5_damage_vehicle/Damage_Vehicle_Y5.pt $WEIGHTS_STORE_ROOT/yolo5_damage_vehicle/pytorch_model.pt

for url in ${YOLOS_LICENSE_PLATE_DETECTION_URLS[@]}; do
  curl -OL --create-dirs --output-dir $WEIGHTS_STORE_ROOT/yolos_license_plate_detection $url
done

for url in ${TROCR_SMALL_PRINTED[@]}; do
  curl -OL --create-dirs --output-dir $WEIGHTS_STORE_ROOT/trocr_small_printed $url
done