package edu.bu.cs551.team8.pipelines;

import edu.bu.cs551.team8.ComplexMLBenchmark;

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
