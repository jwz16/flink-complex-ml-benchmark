package edu.bu.cs551.team8.exceptions;

public class PipelineNotInitializedException extends Exception {

  private static final long serialVersionUID = 2262462916384696841L;
  
  public PipelineNotInitializedException(String errMsg) {
    super(errMsg);
  }
}
