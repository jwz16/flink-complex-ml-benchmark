package edu.bu.flink_complex_ml_benchmark.experiments.handlers.pipeline6;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.handlers.BaseModelHandler;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class VehicleClassificationHandler extends BaseModelHandler  {

  private static final long serialVersionUID = -3207800286428074526L;

  public VehicleClassificationHandler() {}

  public VehicleClassificationHandler(ModelNode modelNode) {
    super(modelNode);
  }

  @Override
  protected MLEvent preprocess(MLEvent input) {
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
    // e.g. {'is_vehicle': true} etc.
    return output;
  }
  
}
