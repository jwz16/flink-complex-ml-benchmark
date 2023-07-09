package edu.bu.flink_complex_ml_benchmark;

import java.io.IOException;
import edu.bu.flink_complex_ml_benchmark.pipelines.Pipeline;

public class ComplexMLBenchmark {
  public enum Type {
    EMBEDDED, EXTERNAL
  }

  public ComplexMLBenchmark(String[] args) throws IOException {
  }

  public void run() throws Exception {
    var config = Config.getInstance();
    
    var p = Pipeline.build(
      config.getBenchmarkType(),
      config.getPipelineType(),
      config.getPipelineConfigPath()
    );

    p.run();
  }

  public static void main(String[] args) throws Exception {
    new ComplexMLBenchmark(args).run();
  }

}