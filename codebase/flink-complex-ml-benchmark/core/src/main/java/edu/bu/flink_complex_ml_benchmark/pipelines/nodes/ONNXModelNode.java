package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import ai.onnxruntime.OrtException;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;
import edu.bu.flink_complex_ml_benchmark.models.ONNXModel;

public class ONNXModelNode extends EmbeddedModelNode {

  private static final long serialVersionUID = 7020531963073250358L;
  
  private ONNXModel model = null;

  public ONNXModelNode(PipelineNode node) {
    super(node);
  }

  @Override
  public void open() {
    try {
      this.model = new ONNXModel(model_path);
    } catch (OrtException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      this.model.close();
    } catch (OrtException e) {
      e.printStackTrace();
    }
  }

  /**
   * ONNX model process function
   * @param input
   */
  @Override
  public MLEventOut process(MLEventIn input) {
    super.process(input);

    if (input.getDataAsINDArray() == null) {
      return input.toMLEventOut();
    }

    var eventOut = input.toMLEventOut();
    try {
      String result = this.model.inference(input);
      eventOut.setResult(result);
      eventOut.setData(input.getData());
    } catch (Exception e) {
      e.printStackTrace();

      eventOut.setResult(null);
      eventOut.setData(null);
    }

    return eventOut;
  }

  public ONNXModel getModel() {
    return model;
  }

  public void setModel(ONNXModel model) {
    this.model = model;
  }

}
