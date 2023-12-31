package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;

public class ExternalModelNode extends ModelNode {

  private static final long serialVersionUID = 2350774847394686686L;

  public ExternalModelNode(PipelineNode node) {
    super(node);
  }

  /**
   * External model process function
   * @param input
   */
  @Override
  public MLEvent process(MLEvent input) {
    return super.process(input);
  }
  
}
