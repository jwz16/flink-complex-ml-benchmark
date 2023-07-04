package edu.bu.cs551.team8;

import java.io.IOException;
import edu.bu.cs551.team8.pipelines.Pipeline;

public class ComplexMLBenchmark {
  public enum Type {
    EMBEDDED, EXTERNAL
  }

  public ComplexMLBenchmark(String[] args) throws IOException {
  }

  private void run() throws Exception {
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