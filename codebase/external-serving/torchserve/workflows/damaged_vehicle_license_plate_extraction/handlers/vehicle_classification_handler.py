from abc import ABC
import torch
import transformers
import base64
import ast
import logging
import io
from PIL import Image
import numpy as np
import json

from ts.torch_handler.base_handler import BaseHandler
from transformers import AutoImageProcessor, AutoModelForImageClassification

logger = logging.getLogger(__name__)
logger.info('Transformers version %s', transformers.__version__)

class VehicleClassificationHandler(BaseHandler, ABC):
  '''
  Transformers handler class for vehicle classification.
  '''

  def __init__(self):
    super(VehicleClassificationHandler, self).__init__()
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
    self.img_processor = AutoImageProcessor.from_pretrained(self.model_dir)
    self.model = AutoModelForImageClassification.from_pretrained(self.model_dir)
    self.model.to(self.device)

    self.model.eval()
    self.model.share_memory()
    logger.info('Transformer model from path %s loaded successfully', self.model_dir)

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

    for img_dict in input_data:
      img_bytes = base64.b64decode(img_dict['bytes'])
      img_arr = np.frombuffer(img_bytes, dtype=np.uint8)
      img_arr = img_arr.reshape(img_dict['shape'])
      images_batch.append(img_arr)

    return self.img_processor(images=images_batch, return_tensors='pt')

  def inference(self, data):
    '''
    Args:
      data (list): List of Image Tensors from the pre-process function is passed here
    Returns:
      list : It returns a list of the predicted value for the input image
    '''
    return self.model(**data)

  def postprocess(self, inference_output):
    '''Post Process Function converts the predicted response into Torchserve readable format.
    Args:
      inference_output: It contains the predicted response of the input images batch.
    Returns:
      (list): Returns the classification results.
    '''

    logits = inference_output.logits

    resp = []
    for logit in logits:
      predicted_class_idx_top3 = torch.flip(logit.argsort(), dims=(0,))[:3]
      predicted_class_top3 = list(map(lambda x : self.model.config.id2label[x.item()], predicted_class_idx_top3))
      resp.append(
        {
          'predicted_classes': predicted_class_top3,
          'is_vehicle': self._is_vehicle(predicted_class_top3)
        }
      )
    
    logger.info(resp)
    return [resp]
  
  def _is_vehicle(self, labels):
    keywords = ['car', 'truck', 'pickup', 'ambulance', 'wagon', 'minivan']
    contains_vehicle = False
    for label in labels:
      for w in label.split(' '):
        if w in keywords:
          contains_vehicle = True
          break
    
    return contains_vehicle