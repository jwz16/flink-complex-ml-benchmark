package edu.bu.cs551.team8.pipelines;

import edu.bu.cs551.team8.ComplexMLBenchmark;

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
        break;
      case ND4J:
        break;
      default:
        break;
    }

    return null;
  }
  
}
