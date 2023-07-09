package edu.bu.flink_complex_ml_benchmark.pipelines;

import edu.bu.flink_complex_ml_benchmark.ComplexMLBenchmark;

public class ExternalPipeline extends Pipeline {

  public ExternalPipeline(Pipeline p) {
    super(p);
  }

  public static ExternalPipeline build(ComplexMLBenchmark.Type benchmarkType, Pipeline.Type pipelineType, String configPath) {
    switch (pipelineType) {
      case TORCH_SERVE:
        return new TorchServePipeline(benchmarkType, Pipeline.build(configPath, pipelineType));
      default:
        break;
    }

    return null;
  }
}
