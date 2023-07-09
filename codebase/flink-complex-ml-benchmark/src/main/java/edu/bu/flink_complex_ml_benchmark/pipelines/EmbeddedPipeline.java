package edu.bu.flink_complex_ml_benchmark.pipelines;

import edu.bu.flink_complex_ml_benchmark.ComplexMLBenchmark;

public class EmbeddedPipeline extends Pipeline {

  public EmbeddedPipeline(Pipeline p) {
    super(p);
  }

  public static EmbeddedPipeline build(
    ComplexMLBenchmark.Type benchmarkType,
    Pipeline.Type pipelineType,
    String configPath
  ) {
    
    switch (pipelineType) {
      case ONNX:
        return new ONNXPipeline(benchmarkType, Pipeline.build(configPath, pipelineType));
      case TF_SAVED_MODEL:
        // TODO
        break;
      case ND4J:
        // TODO
        break;
      default:
        break;
    }

    return null;
  }
  
}
