package edu.bu.cs551.team8.exceptions;

public class UnknownPipelineType extends Exception {

  private static final long serialVersionUID = -3209279730602626987L;

  public UnknownPipelineType(String errMsg) {
    super(errMsg);
  }

}
