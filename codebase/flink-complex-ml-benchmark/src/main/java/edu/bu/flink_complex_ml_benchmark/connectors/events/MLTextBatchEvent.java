package edu.bu.flink_complex_ml_benchmark.connectors.events;

public class MLTextBatchEvent extends MLEvent {

  private static final long serialVersionUID = -8960998334482751899L;

  public MLTextBatchEvent(long id, long timestamp) {
    super(id, timestamp);
  }
  
}
