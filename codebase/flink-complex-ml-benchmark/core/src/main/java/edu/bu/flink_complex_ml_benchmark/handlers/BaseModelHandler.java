package edu.bu.flink_complex_ml_benchmark.handlers;

import java.io.Serializable;
import java.util.Collections;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.Collector;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class BaseModelHandler implements Serializable {

  private static final long serialVersionUID = 4421609248097925816L;
  
  protected RichAsyncFunction<MLEventIn, MLEventOut> asyncFunction = new ModelAsyncFunction(this);  // For external
  protected ProcessFunction<MLEventIn, MLEventOut> syncFunction = new ModelSyncFunction(this);      // For embedded

  protected ModelNode modelNode;

  public BaseModelHandler() {}
  
  public BaseModelHandler(ModelNode modelNode) {
    this.modelNode = modelNode;
  }

  public RichAsyncFunction<MLEventIn, MLEventOut> toAsyncFunction() {
    return asyncFunction;
  }

  public ProcessFunction<MLEventIn, MLEventOut> toSyncFunction() {
    return syncFunction;
  }

  protected MLEventIn preprocess(MLEventIn input) {
    return input;
  }

  protected MLEventOut inference(MLEventIn input) throws Exception {
    return modelNode.process(input);
  }

  protected MLEventOut postprocess(MLEventOut output) {
    return output;
  }

  protected MLEventOut process(MLEventIn input) {
    var inputPrep = preprocess(input);

    var result = new MLEventOut();
    try {
      result = inference(inputPrep);
    } catch (Exception e) {
      e.printStackTrace();
    }

    var eventOut = postprocess(result);

    return eventOut;
  }

  protected class ModelAsyncFunction extends RichAsyncFunction<MLEventIn, MLEventOut> {

    private static final long serialVersionUID = 4467786009847331338L;

    private final BaseModelHandler handler;

    public ModelAsyncFunction(BaseModelHandler handler) {
      super();

      this.handler = handler;
    }

    @Override
    public void asyncInvoke(MLEventIn input, ResultFuture<MLEventOut> resultFuture) {
      var eventOut = handler.process(input);
      resultFuture.complete(Collections.singleton(eventOut));
    }
    
  }

  protected class ModelSyncFunction extends ProcessFunction<MLEventIn, MLEventOut> {

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
    public void processElement(MLEventIn input, ProcessFunction<MLEventIn, MLEventOut>.Context ctx, Collector<MLEventOut> collector) {
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
