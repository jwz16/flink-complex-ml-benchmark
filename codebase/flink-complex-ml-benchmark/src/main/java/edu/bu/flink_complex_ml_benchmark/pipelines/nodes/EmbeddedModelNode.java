package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;

public class EmbeddedModelNode extends ModelNode {

  private static final long serialVersionUID = 8292380528451476039L;
  
  public EmbeddedModelNode(PipelineNode node) {
    super(node);
  }

  /**
   * Embedded model process function
   * @param input
   */
  @Override
  public MLEventOut process(MLEventIn input) {
    return super.process(input);
  }

}