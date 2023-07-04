from abc import ABC
import torch
import transformers
import base64
import ast
import logging
import io
from PIL import Image
import numpy as np

from ts.torch_handler.base_handler import BaseHandler
from transformers import YolosImageProcessor, YolosForObjectDetection

logger = logging.getLogger(__name__)
logger.info('Transformers version %s', transformers.__version__)

class VehicleDetectionHandler(BaseHandler, ABC):
  '''
  Transformers handler class for vehicle detection.
  '''

  def __init__(self):
    super(VehicleDetectionHandler, self).__init__()
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
    self.img_processor = YolosImageProcessor.from_pretrained(self.model_dir)
    self.model = YolosForObjectDetection.from_pretrained(self.model_dir)
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

    if data is None:
      return None

    input_data = data[0].get('data') or data[0].get('body')
    if isinstance(input_data, str):
      # if the image is a string of bytesarray.
      print('got an image as string')
      images_batch = base64.b64decode(input_data).decode()
      images_batch = ast.literal_eval(images_batch)
      images_batch = np.array(images_batch).astype(np.float32)
      images_batch = torch.from_numpy(images_batch)
      images_batch = images_batch.to(self.device)
    if isinstance(input_data, (bytearray, bytes)):
      # if the image is sent as bytesarray
      print('got an image as bytearray or bytes')
      images_batch = [Image.open(io.BytesIO(input_data))]

    return self.img_processor(images=images_batch, return_tensors='pt')

  def inference(self, input_batch):
    '''
    Args:
      input_batch (list): List of Image Tensors from the pre-process function is passed here
    Returns:
      list : It returns a list of the predicted value for the input image
    '''

    if input_batch is None:
      return None

    self.input_batch = input_batch
    return self.model(**input_batch)

  def postprocess(self, inference_output):
    '''Post Process Function converts the predicted response into Torchserve readable format.
    Args:
      inference_output (list): It contains the predicted response of the input images batch.
    Returns:
      (list): Returns the detected results.
    '''

    if inference_output is None:
      return None

    target_sizes = list(map(lambda x : x.shape[1:], self.input_batch['pixel_values']))
    results = self.img_processor.post_process_object_detection(inference_output, threshold=0.9, target_sizes=target_sizes)
    results_np = list(map(lambda r: {
      'scores': r['scores'].tolist(),
      'labels': r['labels'].tolist(),
      'boxes': r['boxes'].tolist(),
      }, results))
    return results_np