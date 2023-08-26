package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;

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
  public MLEvent process(MLEvent input) {
    return super.process(input);
  }

}
