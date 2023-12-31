package edu.bu.flink_complex_ml_benchmark.experiments.handlers.pipeline4;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.handlers.BaseModelHandler;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class DamagedVehicleDetectionHandler extends BaseModelHandler {

  private static final long serialVersionUID = -7720124263967175880L;
  
  public DamagedVehicleDetectionHandler() {}

  public DamagedVehicleDetectionHandler(ModelNode modelNode) {
    super(modelNode);
  }

  @Override
  protected MLEvent preprocess(MLEvent input) {
    // TODO: should perform some model specific logic here.
    // input event result should be a JSON string
    // input.getResults().get('model_name') will give the specific output from the upstream model.
    // e.g. check if the input is a vehicle by inspect {'is_vehicle': true} etc.
    return input;
  }

  @Override
  protected MLEvent inference(MLEvent input) {
    try {
      return modelNode.process(input);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new MLEvent();
  }

  @Override
  protected MLEvent postprocess(MLEvent output) {
    // TODO: should perform some model specific logic here.
    // output event result should be a JSON string
    // e.g. {'bounding_boxes': [...]} etc.
    // e.g. crop data(image) with the bounding boxes

    return output;
  }
  
}
