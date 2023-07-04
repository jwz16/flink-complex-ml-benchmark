package edu.bu.cs551.team8.models;

import java.io.IOException;
import java.util.Collections;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import edu.bu.cs551.team8.connectors.events.MLImageFileBatchEvent;

public class ONNXModel  {
  private static Logger logger = LoggerFactory.getLogger(ONNXModel.class);

  public OrtEnvironment env;
  public OrtSession session;
  public String modelPath;

  public ONNXModel(String modelPath) throws OrtException {
    loadModelFile(modelPath);
  }

  public void close() throws OrtException {
    // DO NOT CHANGE THE ORDER OF THESE TWO LINES!
    this.session.close();
    this.env.close();
  }

  private void loadModelFile(String modelPath) throws OrtException {
    this.modelPath = getClass().getClassLoader().getResource(modelPath).getPath();
    this.env = OrtEnvironment.getEnvironment();
    this.session = env.createSession(this.modelPath, new OrtSession.SessionOptions());
  }
  
  public String inference(MLImageFileBatchEvent event) {
    try {
      var inputShape = ((TensorInfo)session.getInputInfo().get("input").getInfo()).getShape();

      var imgStream = getClass().getClassLoader().getResourceAsStream(event.getBatch().get(0));
      var imgLoader = new NativeImageLoader(inputShape[3], inputShape[2], inputShape[1]);
      var inputMat = imgLoader.asMatrix(imgStream);
      var floatBuf = inputMat.data().asNioFloat();

      long[] shape = new long[]{ event.getBatch().size(), inputShape[1], inputShape[2], inputShape[3] };
      var inputTensor = OnnxTensor.createTensor(this.env, floatBuf, shape);
      var output = session.run(Collections.singletonMap("input", inputTensor));
      
      var preds = Nd4j.create(
        ((OnnxTensor)output.get(1)).getFloatBuffer().array(),
        new long[] { 1, 768 },
        'c'
      );

    } catch (IOException | OrtException e) {
      e.printStackTrace();
    }

    return null;
  }
  
}
