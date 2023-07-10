#!/usr/bin/env python3

import torch.onnx
from transformers import AutoModelForImageClassification, YolosForObjectDetection, AutoTokenizer, VisionEncoderDecoderModel
import yolov5

def convert(model, model_name, image_size=[3, 224, 224], decoder_input_ids=None, dynamic_axes=None):
  output_dir = '../assets/weights/onnx'
  batch_size = 1

  # Input to the model
  x = torch.randn(batch_size, image_size[0], image_size[1], image_size[2], requires_grad=True)
  input = x if decoder_input_ids is None else (x, decoder_input_ids)

  if dynamic_axes is None:
    dynamic_axes = {
      'input' : {0 : 'batch_size'},
      'output' : {0 : 'batch_size'}
    }

  # Export the model
  torch.onnx.export(model,                                          # model being run
                    input,                                          # model input (or a tuple for multiple inputs)
                    f'{output_dir}/{model_name}.onnx',              # where to save the model (can be a file or file-like object)
                    export_params=True,                             # store the trained parameter weights inside the model file
                    opset_version=12,                               # the ONNX version to export the model to
                    do_constant_folding=True,                       # whether to execute constant folding for optimization
                    input_names = ['input'],                        # the model's input names
                    output_names = ['output'],                      # the model's output names
                    dynamic_axes = dynamic_axes)                    # variable length axes

if __name__ == '__main__':
    torch_model = AutoModelForImageClassification.from_pretrained('../assets/weights/pytorch/mobilenet_v1')
    torch_model.eval()
    convert(torch_model, 'mobilenet_v1')

    torch_model = yolov5.load('../assets/weights/pytorch/yolo5_damage_vehicle/pytorch_model.pt')
    torch_model.eval()
    convert(torch_model, 'yolo5_damage_vehicle')

    torch_model = YolosForObjectDetection.from_pretrained('../assets/weights/pytorch/yolos_license_plate_detection')
    torch_model.eval()
    convert(torch_model, 'yolos_license_plate_detection')

    tokenizer = AutoTokenizer.from_pretrained('../assets/weights/pytorch/trocr_small_printed')
    decoder_input_ids = tokenizer("<s>", return_tensors="pt")["input_ids"]
    torch_model = VisionEncoderDecoderModel.from_pretrained('../assets/weights/pytorch/trocr_small_printed')
    torch_model.eval()
    convert(torch_model, 'trocr_small_printed', image_size=[3, 384, 384], decoder_input_ids=decoder_input_ids)