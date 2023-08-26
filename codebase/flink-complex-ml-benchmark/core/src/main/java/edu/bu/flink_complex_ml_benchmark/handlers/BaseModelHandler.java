package edu.bu.flink_complex_ml_benchmark.handlers;

import java.io.Serializable;
import java.util.Collections;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.Collector;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class BaseModelHandler implements Serializable {

  private static final long serialVersionUID = 4421609248097925816L;
  
  protected RichAsyncFunction<MLEvent, MLEvent> asyncFunction = new ModelAsyncFunction(this);  // For external
  protected ProcessFunction<MLEvent, MLEvent> syncFunction = new ModelSyncFunction(this);      // For embedded

  protected ModelNode modelNode;

  public BaseModelHandler() {}
  
  public BaseModelHandler(ModelNode modelNode) {
    this.modelNode = modelNode;
  }

  public RichAsyncFunction<MLEvent, MLEvent> toAsyncFunction() {
    return asyncFunction;
  }

  public ProcessFunction<MLEvent, MLEvent> toSyncFunction() {
    return syncFunction;
  }

  protected MLEvent preprocess(MLEvent input) {
    return input;
  }

  protected MLEvent inference(MLEvent input) throws Exception {
    return modelNode.process(input);
  }

  protected MLEvent postprocess(MLEvent output) {
    return output;
  }

  protected MLEvent process(MLEvent input) {
    var inputPrep = preprocess(input);

    var result = new MLEvent();
    try {
      result = inference(inputPrep);
    } catch (Exception e) {
      e.printStackTrace();
    }

    var eventOut = postprocess(result);

    return eventOut;
  }

  protected class ModelAsyncFunction extends RichAsyncFunction<MLEvent, MLEvent> {

    private static final long serialVersionUID = 4467786009847331338L;

    private final BaseModelHandler handler;

    public ModelAsyncFunction(BaseModelHandler handler) {
      super();

      this.handler = handler;
    }

    @Override
    public void asyncInvoke(MLEvent input, ResultFuture<MLEvent> resultFuture) {
      var eventOut = handler.process(input);
      resultFuture.complete(Collections.singleton(eventOut));
    }
    
  }

  protected class ModelSyncFunction extends ProcessFunction<MLEvent, MLEvent> {

    private static final long serialVersionUID = -4433846089452226319L;

    private final BaseModelHandler handler;

    public ModelSyncFunction(BaseModelHandler handler) {
      super();

      this.handler = handler;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
      modelNode.open();
    }
  
    @Override
    public void close() throws Exception {
      modelNode.close();
    }

    @Override
    public void processElement(MLEvent input, ProcessFunction<MLEvent, MLEvent>.Context ctx, Collector<MLEvent> collector) {
      var eventOut = handler.process(input);
      collector.collect(eventOut);
    }

  }

  public ModelNode getModelNode() {
    return modelNode;
  }

  public void setModelNode(ModelNode modelNode) {
    this.modelNode = modelNode;
  }

}
