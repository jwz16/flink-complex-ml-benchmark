package edu.bu.cs551.team8.pipelines.nodes;

import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLEventOut;

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
