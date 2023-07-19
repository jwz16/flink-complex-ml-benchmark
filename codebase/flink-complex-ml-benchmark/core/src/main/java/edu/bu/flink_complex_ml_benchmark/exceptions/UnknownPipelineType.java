package edu.bu.flink_complex_ml_benchmark.exceptions;

public class UnknownPipelineType extends Exception {

  private static final long serialVersionUID = -3209279730602626987L;

  public UnknownPipelineType(String errMsg) {
    super(errMsg);
  }

}
