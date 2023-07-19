package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;
import edu.bu.flink_complex_ml_benchmark.handlers.BaseModelHandler;
import edu.bu.flink_complex_ml_benchmark.pipelines.Pipeline;

public class ModelNode extends PipelineNode {
  
  private static final long serialVersionUID = 3631964938240243132L;

  private Pipeline.Type framework;

  protected BaseModelHandler handler = null;

  public ModelNode(PipelineNode node) {
    super(node);
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

  public void open() {
    throw new UnsupportedOperationException("Should not reach here, you need to override this method.");
  }

  public void close() {
    throw new UnsupportedOperationException("Should not reach here, you need to override this method.");
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

  public Pipeline.Type getFramework() {
    return framework;
  }

  public void setFramework(Pipeline.Type framework) {
    this.framework = framework;
  }

}
