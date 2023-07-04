package edu.bu.cs551.team8.pipelines.nodes;


import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLEventOut;
import edu.bu.cs551.team8.handlers.BaseModelHandler;

public class ModelNode extends PipelineNode {
  
  private static final long serialVersionUID = 3631964938240243132L;

  protected BaseModelHandler handler = null;

  public ModelNode(PipelineNode node) {
    super(node);
  }

  public BaseModelHandler getHandler() {
    return handler;
  }

  public void setHandler(BaseModelHandler handler) {
    if (handler == null)
      return;
    
    this.handler = handler;
    this.handler.setModelNode(this);
  }

  
  /**
   * Base model process function
   * @param input
   */
  public MLEventOut process(MLEventIn input) {
    checkRecordStartTimestamp(input);
    return null;
  }

  private void checkRecordStartTimestamp(MLEventIn e) {
    if (!e.isStarted()) {
      e.setStarted(true);
      e.setStartTimestamp(System.nanoTime());
    }
  }

}
