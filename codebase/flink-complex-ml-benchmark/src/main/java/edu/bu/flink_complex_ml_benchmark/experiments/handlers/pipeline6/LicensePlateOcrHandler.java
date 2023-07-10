package edu.bu.flink_complex_ml_benchmark.experiments.handlers.pipeline6;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;
import edu.bu.flink_complex_ml_benchmark.handlers.BaseModelHandler;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class LicensePlateOcrHandler extends BaseModelHandler {

  private static final long serialVersionUID = -8681796604151723875L;
  
  public LicensePlateOcrHandler() {}

  public LicensePlateOcrHandler(ModelNode modelNode) {
    super(modelNode);
  }

  @Override
  protected MLEventIn preprocess(MLEventIn input) {
    // TODO: should perform some model specific logic here.
    // input event result should be a JSON string
    // input.getResults().get('model_name') will give the specific output from the upstream model.
    // e.g. check if the input is a vehicle by inspect {'is_vehicle': true} etc.
    return input;
  }

  @Override
  protected MLEventOut inference(MLEventIn input) {
    try {
      return modelNode.process(input);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new MLEventOut();
  }

  @Override
  protected MLEventOut postprocess(MLEventOut output) {
    // TODO: should perform some model specific logic here.
    // output event result should be a JSON string
    // e.g. {'bounding_boxes': [...]} etc.
    // e.g. crop data(image) with the bounding boxes

    return output;
  }

}
