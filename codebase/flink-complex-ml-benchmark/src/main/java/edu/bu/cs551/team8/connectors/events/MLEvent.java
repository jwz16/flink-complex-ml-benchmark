package edu.bu.cs551.team8.connectors.events;

import java.io.Serializable;

/**
 * Machine Learning Event class
 */
public class MLEvent implements Serializable {

  private static final long serialVersionUID = -2796348114926512035L;
  protected long id;
  protected long timestamp;       // timestamp when the event is created at.
  protected long startTimestamp;  // timestamp when the event is processed by the first model.
  protected long finishTimestamp; // timestamp when the event goes through the whole pipeline.
  protected boolean started;    // is event processed by the first model?
  protected byte[] data;

  public MLEvent() {}

  public MLEvent(long id, long timestamp) {
    this.id = id;
    this.timestamp = timestamp;
    this.started = false;
  }
  
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(long startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public long getFinishTimestamp() {
    return finishTimestamp;
  }

  public void setFinishTimestamp(long finishTimestamp) {
    this.finishTimestamp = finishTimestamp;
  }

  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean started) {
    this.started = started;
  }
  
}
