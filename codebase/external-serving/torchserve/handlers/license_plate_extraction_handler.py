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
from torchvision import transforms

from ts.torch_handler.base_handler import BaseHandler
from transformers import YolosImageProcessor, YolosForObjectDetection

logger = logging.getLogger(__name__)
logger.info('Transformers version %s', transformers.__version__)

torch.set_num_threads(1)

class LicensePlateExtractionHandler(BaseHandler, ABC):
  '''
  Transformers handler class for license plate extraction.
  '''

  def __init__(self):
    super(LicensePlateExtractionHandler, self).__init__()
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

    images_batch = []
    input_data = data[0].get('data') or data[0].get('body')
    input_data = json.loads(input_data.decode())
    images_batch_bytes = base64.b64decode(input_data['data'])
    shape = input_data['shape']

    img_arr = np.frombuffer(images_batch_bytes, dtype=np.uint8, offset=128)
    img_arr = img_arr.reshape(shape)

    for img in img_arr:
      images_batch.append(img)

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
      return [ {'error': 'cannot detect the license plate!'} ]

    target_sizes = list(map(lambda x : x.shape[1:], self.input_batch['pixel_values']))
    results = self.img_processor.post_process_object_detection(inference_output, threshold=0.9, target_sizes=target_sizes)

    if len(results) == 0:
      return [ {'error': 'cannot detect the license plate!'} ]

    results_np = list(
      map(
        lambda r: {
          'scores': r['scores'].tolist(),
          'labels': r['labels'].tolist(),
          'boxes': r['boxes'].tolist(),
        },
        results
      )
    )

    for img, r in zip(self.input_batch['pixel_values'], results_np):
      r['cropped_imgs'] = self._crop_image(img, r)
    
    # logger.info(results_np)
    return [results_np]
  
  def _has_damaged_vehicles(self, result):
    if 'error' in result:
      return False

    return result['conf'] > 0.3
  
  def _crop_image(self, img, result):
    return list(
      map(
        lambda box : {
          'bytes': base64.b64encode(
                      transforms.functional.crop(img, int(box[0]), int(box[1]), int(box[2]-box[0]), int(box[3]-box[1]))  # box=[x, y, x+w, y+h]
                                           .detach()
                                           .numpy()
                                           .astype(np.uint8)
                                           .tobytes()
                   ).decode(),
          'shape': [img.shape[0], int(box[2]-box[0]), int(box[3]-box[1])]
        },
        result['boxes']
      )
    )