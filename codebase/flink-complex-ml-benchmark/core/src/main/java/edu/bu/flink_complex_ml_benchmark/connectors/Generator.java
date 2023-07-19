package edu.bu.flink_complex_ml_benchmark.connectors;

import java.io.Serializable;
import java.util.Iterator;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;

public abstract class Generator implements Iterator<MLEventIn>, Serializable {

  private static final long serialVersionUID = 6572714416509800940L;

  @Override
  public boolean hasNext() {
    throw new UnsupportedOperationException("Unimplemented method 'hasNext'");
  }

  @Override
  public MLEventIn next() {
    throw new UnsupportedOperationException("Unimplemented method 'next'");
  }

  public abstract long getEventId();
  public abstract void setEventId(long eventId);
  
}
