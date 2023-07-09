package edu.bu.flink_complex_ml_benchmark.exceptions;

public class PipelineNotInitializedException extends Exception {

  private static final long serialVersionUID = 2262462916384696841L;
  
  public PipelineNotInitializedException(String errMsg) {
    super(errMsg);
  }
}
