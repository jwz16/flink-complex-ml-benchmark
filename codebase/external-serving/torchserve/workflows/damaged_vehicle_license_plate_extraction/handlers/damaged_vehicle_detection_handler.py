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
    classify_rsps = json.loads(data[0].get('vehicle_classification').decode())
    img_dicts = json.loads(data[0].get('pre_processing').decode())

    for classify_result, img_dict in zip(classify_rsps, img_dicts):
      if classify_result['is_vehicle'] is True:
        # get original image data if it is a vehicle.
        img_bytes = base64.b64decode(img_dict['bytes'])
        img_arr = np.frombuffer(img_bytes, dtype=np.uint8)
        img_arr = img_arr.reshape(img_dict['shape'])
        images_batch.append(img_arr)
    
    if len(images_batch) == 0:
      return None

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

    logger.info(resp)
    
    return [resp]