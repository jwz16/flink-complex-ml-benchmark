package edu.bu.cs551.team8.pipelines;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import edu.bu.cs551.team8.ComplexMLBenchmark;
import edu.bu.cs551.team8.connectors.MLRepeatedImageBatchesSource;
import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.models.ONNXModel;
import edu.bu.cs551.team8.pipelines.nodes.ONNXModelNode;

public class ONNXPipeline extends EmbeddedPipeline {
  
  public ONNXPipeline(ComplexMLBenchmark.Type benchmarkType, Pipeline p) {
    super(p);

    this.benchmarkType = benchmarkType;
    this.type = Type.ONNX;
  }

  @Override
  public void run() throws Exception {
    // Set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.getConfig().registerKryoType(ONNXModel.class);
    env.setParallelism(1); // use 1 processing tasks

    String testImagePath = "test_data/1.jpeg";
    DataStream<MLEventIn> imageBatches = env
            .addSource(new MLRepeatedImageBatchesSource(testImagePath, 10, 100, 50, 10));
    
    // imageBatches.process((ONNXModelNode)this.nodes.get(0)).print();

    env.execute(this.toString());
  }

  @Override
  public String toString() {
    return "ONNXPipeline";
  }

}
