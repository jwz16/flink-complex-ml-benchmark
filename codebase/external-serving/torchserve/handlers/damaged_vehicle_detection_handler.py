from abc import ABC
import torch
import base64
import logging
import io
import ast
from PIL import Image
import numpy as np
import json
import yolov5

from ts.torch_handler.base_handler import BaseHandler

logger = logging.getLogger(__name__)

torch.set_num_threads(1)

class DamagedVehicleDetectionHandler(BaseHandler, ABC):
  '''
  Yolo5 handler class for damage vehicle detection.
  '''

  def __init__(self):
    super(DamagedVehicleDetectionHandler, self).__init__()
    self.initialized = False
    self.ctx = ''

  def initialize(self, ctx):
    '''
    Args:
      ctx (context): It is a JSON Object containing information
      pertaining to the model artefacts parameters.
    '''
    self.ctx = ctx
    self.manifest = ctx.manifest
    properties = ctx.system_properties
    self.model_dir = properties.get('model_dir')

    self.device = torch.device('cpu')
    self.model = yolov5.load(self.model_dir + '/pytorch_model.pt', device=self.device)

    # Setting model configuration 
    self.model.conf = 0.35
    self.model.iou = 0.45

    self.model.eval()
    self.model.share_memory()
    logger.info('Yolo5 model from path %s loaded successfully', self.model_dir)

    self.initialized = True

  def preprocess(self, data):
    '''Basic image preprocessing
    Args:
      data (str): The Input data in the form of image is passed on to the preprocess
      function.
    Returns:
      list : The preprocess function returns a list of Tensor.
    '''
    
    images_batch = []
    input_data = data[0].get('data') or data[0].get('body')
    input_data = json.loads(input_data.decode())
    images_batch_bytes = base64.b64decode(input_data['data'])
    shape = input_data['shape']

    img_arr = np.frombuffer(images_batch_bytes, dtype=np.uint8, offset=128)
    img_arr = img_arr.reshape(shape)
    
    for img in img_arr:
      images_batch.append(img)

    return images_batch

  def inference(self, input_batch):
    '''
    Args:
      input_batch (list): List of Image Tensors from the pre-process function is passed here
    Returns:
      list : It returns a list of the predicted value for the input image
    '''

    if input_batch is None:
      return None
    
    return self.model(input_batch, size=640)

  def postprocess(self, inference_output):
    '''Post Process Function converts the predicted response into Torchserve readable format.
    Args:
      inference_output: It contains the predicted response of the input images batch.
    Returns:
      (list): Returns the detected results.
    '''

    if inference_output is None:
      return [ {'error': 'cannot detect damaged vehicle!'} ]
    
    crops = inference_output.crop(save=False)

    if len(crops) == 0:
      return [ {'error': 'cannot detect damaged vehicle!'} ]

    resp = list(
      map(lambda crop : {
        'box': list(map(lambda x: x.item(), crop['box'])),
        'conf': crop['conf'].item(),
        'label': crop['label']
      }, crops)
    )

    # logger.info(resp)
    
    return [resp]