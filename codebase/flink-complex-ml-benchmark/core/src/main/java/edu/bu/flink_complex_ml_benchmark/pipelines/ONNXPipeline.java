package edu.bu.flink_complex_ml_benchmark.pipelines;

import java.util.Set;

import org.apache.flink.streaming.api.datastream.DataStream;

import edu.bu.flink_complex_ml_benchmark.ComplexMLBenchmark;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.models.ONNXModel;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;

public class ONNXPipeline extends EmbeddedPipeline {
  
  public ONNXPipeline(ComplexMLBenchmark.Type benchmarkType, Pipeline p) {
    super(p);

    this.benchmarkType = benchmarkType;
    this.type = Type.ONNX;
  }

  @Override
  public void run() throws Exception {
    setupStream();

    env.getConfig().registerKryoType(ONNXModel.class);

    resultStream.sinkTo(sink);

    // Execute the program
    env.execute("Executing " + this.toString());
  }

  @Override
  protected DataStream<MLEvent> modelStreamOp(ModelNode node, Set<DataStream<MLEvent>> attachedInputStreams) {
    return mergeStreams(attachedInputStreams).process(node.getHandler().toSyncFunction());
  }

  @Override
  public String toString() {
    return "ONNXPipeline";
  }

}
