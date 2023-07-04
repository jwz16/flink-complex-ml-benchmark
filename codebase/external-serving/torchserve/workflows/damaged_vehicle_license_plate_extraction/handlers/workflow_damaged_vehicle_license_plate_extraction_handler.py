#!/usr/bin/python3

import json
import base64
import ast
import io
import numpy as np
from PIL import Image

def pre_processing(data, context):
  '''
  Empty node as a starting node since the DAG doesn't support multiple start nodes
  '''
  
  if data is None:
    return None

  input_data = data[0].get("data") or data[0].get("body")
  
  # Base64 encode the images to avoid the framework throwing
  # non json encodable errors
  b64_data = []

  print(context.request_processor)

  if isinstance(input_data, str):
    # received batch image data from Flink
    img_data = input_data.decode('utf-8')
    img_data = ast.literal_eval(img_data)               # should be list type
    img_arr = np.array(img_data, dtype=np.uint8)
    b64_data.append(
      {
        'bytes': base64.b64encode(img_arr.tobytes()).decode(),
        'shape': img_arr.shape
      }
    )
  if isinstance(input_data, (bytearray, bytes)):
    # received a single image from HTTP api
    img = Image.open(io.BytesIO(input_data))
    img_arr = np.asarray(img, dtype=np.uint8)
    b64_data.append(
      {
        'bytes': base64.b64encode(img_arr.tobytes()).decode(),
        'shape': img_arr.shape
      }
    )
  return [b64_data]