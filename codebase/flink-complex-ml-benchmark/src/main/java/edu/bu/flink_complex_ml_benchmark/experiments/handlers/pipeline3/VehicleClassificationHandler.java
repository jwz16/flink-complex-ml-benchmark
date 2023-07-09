package edu.bu.flink_complex_ml_benchmark.experiments.handlers.pipeline3;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;
import edu.bu.flink_complex_ml_benchmark.handlers.BaseModelHandler;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class VehicleClassificationHandler extends BaseModelHandler  {

  private static final long serialVersionUID = -3207800286428074526L;

  public VehicleClassificationHandler() {}

  public VehicleClassificationHandler(ModelNode modelNode) {
    super(modelNode);
  }

  @Override
  protected MLEventIn preprocess(MLEventIn input) {
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
    // e.g. {'is_vehicle': true} etc.
    return output;
  }
  
}
