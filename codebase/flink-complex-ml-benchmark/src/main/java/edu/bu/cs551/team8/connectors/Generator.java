package edu.bu.cs551.team8.connectors;

import java.io.Serializable;
import java.util.Iterator;

import edu.bu.cs551.team8.connectors.events.MLEventIn;

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
