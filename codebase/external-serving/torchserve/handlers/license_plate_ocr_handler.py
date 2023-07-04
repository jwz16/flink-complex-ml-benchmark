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
from transformers import TrOCRProcessor, VisionEncoderDecoderModel

logger = logging.getLogger(__name__)
logger.info('Transformers version %s', transformers.__version__)

torch.set_num_threads(1)

class LicensePlateOcrHandler(BaseHandler, ABC):
  '''
  Transformers handler class for text recognition.
  '''

  def __init__(self):
    super(LicensePlateOcrHandler, self).__init__()
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
    self.processor = TrOCRProcessor.from_pretrained(self.model_dir)
    self.model = VisionEncoderDecoderModel.from_pretrained(self.model_dir)

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
    images_batch_bytes = base64.b64decode(input_data['data'])
    shape = input_data['shape']

    img_arr = np.frombuffer(images_batch_bytes, dtype=np.uint8, offset=128)
    img_arr = img_arr.reshape(shape)

    for img in img_arr:
      images_batch.append(img)

    return self.processor(images=images_batch, return_tensors="pt")

  def inference(self, input_batch):
    '''
    Args:
      input_batch (list): List of Image Tensors from the pre-process function is passed here
    Returns:
      list : It returns a list of the predicted value for the input image
    '''

    if input_batch is None:
      return None

    generated_ids = self.model.generate(input_batch.pixel_values)

    return generated_ids

  def postprocess(self, inference_output):
    '''Post Process Function converts the predicted response into Torchserve readable format.
    Args:
      inference_output (list): It contains the predicted response of the input images batch.
    Returns:
      (list): Returns the detected results.
    '''

    if inference_output is None:
      return [ {'error': 'cannot extract the license plate number from the given image!'} ]

    # logger.info(inference_output)

    generated_ids = inference_output
    generated_texts = self.processor.batch_decode(generated_ids, skip_special_tokens=True)

    # logger.info(generated_texts)

    if len(generated_texts) == 0:
      return [ {'error': 'cannot extract the license plate number from the given image!'} ]

    resp = [
      list(
        map(
          lambda t : { 'generated_text' : t },
          generated_texts
        )
      )
    ]
    return resp