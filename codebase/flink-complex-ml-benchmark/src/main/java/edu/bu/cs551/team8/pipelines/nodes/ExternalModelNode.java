package edu.bu.cs551.team8.pipelines.nodes;

import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLEventOut;

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
  public MLEventOut process(MLEventIn input) {
    return super.process(input);
  }
  
}
