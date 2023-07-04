package edu.bu.cs551.team8;

import java.util.List;
import java.util.Vector;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

@SuppressWarnings("unchecked")
public class DummySink<OUT> implements SinkFunction<OUT> {

  private static final long serialVersionUID = 3352876271229397202L;

  private static final List records = new Vector<>();

  @Override
  public void invoke(OUT value, Context context) throws Exception {
    records.add(value);
  }
  
  public List<OUT> getRecords() {
    return records;
  }

}
