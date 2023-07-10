package edu.bu.flink_complex_ml_benchmark.models;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.nd4j.enums.ImageResizeMethod;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.binary.BinarySerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import edu.bu.flink_complex_ml_benchmark.Config;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;

public class ONNXModel  {
  private static Logger logger = LoggerFactory.getLogger(ONNXModel.class);

  public transient OrtEnvironment env;
  public transient OrtSession session;
  public transient long[] inputShape;
  public transient long[] outputShape;
  public String modelPath;

  public ONNXModel(String modelPath) throws OrtException {
    loadModelFile(modelPath);
  }

  public void close() throws OrtException {
    logger.info("ONNX model unloaded.");

    // DO NOT CHANGE THE ORDER OF THESE TWO LINES!
    this.session.close();
    this.env.close();
  }

  private void loadModelFile(String modelPath) throws OrtException {
    logger.info("Loading ONNX model ...");

    this.modelPath = getClass().getClassLoader().getResource(modelPath).getPath();
    this.env = OrtEnvironment.getEnvironment();
    this.session = env.createSession(this.modelPath, new OrtSession.SessionOptions());

    this.inputShape = ((TensorInfo)session.getInputInfo().get("input").getInfo()).getShape();
    this.outputShape = ((TensorInfo)session.getOutputInfo().get("output").getInfo()).getShape();

    logger.info("Loading ONNX model ... Done");

    showModelShapeInfo();
  }
  
  public String inference(MLEventIn event) {
    logger.info("Inferencing event: " + event.getId());

    try {
      var config = Config.getInstance();

      var inputMat = Nd4j.image().imageResize(
        event.getDataAsINDArray().permute(0, 2, 3, 1).castTo(DataType.DOUBLE), /* swap dimensions to NHWC */
        Nd4j.createFromArray(inputShape[2], inputShape[3]),
        ImageResizeMethod.ResizeBicubic
      );
      inputMat = inputMat.permute(0, 3, 1, 2); // swap dimensions back

      Map<String, OnnxTensor> inputs = new HashMap<>();
      var floatBuf = inputMat.data().asNioFloat();
      var inputTensor = OnnxTensor.createTensor(this.env, floatBuf, inputMat.shape());
      inputs.put("input", inputTensor);
      
      if (session.getInputInfo().size() > 1) {
        var shapeTensor = OnnxTensor.createTensor(this.env, new long[][] {{inputShape[1], inputShape[2], inputShape[3]}});
        inputs.put("onnx::Reshape_1", shapeTensor);
      }

      var output = session.run(inputs);
      
      // var preds = Nd4j.create(
      //   ((OnnxTensor)output.get(1)).getFloatBuffer().array(),
      //   new long[] {config.getBatchSize(), outputShape[1]},
      //   'c'
      // );

      // var buf = BinarySerde.toByteBuffer(preds);
      // var data = new byte[buf.remaining()];
      // buf.get(data);

      // return Base64.getEncoder().encodeToString(data);

      return null;
    } catch (OrtException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void showModelShapeInfo() {
    logger.info("ONNX model input shape: " + Arrays.toString(inputShape));
    logger.info("ONNX model output shape: " + Arrays.toString(outputShape));
  }
  
}
