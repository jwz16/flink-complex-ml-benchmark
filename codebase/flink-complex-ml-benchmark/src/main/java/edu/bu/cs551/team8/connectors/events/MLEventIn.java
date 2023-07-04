package edu.bu.cs551.team8.connectors.events;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.serde.binary.BinarySerde;

public class MLEventIn extends MLEvent {
  private static final long serialVersionUID = 5198233974122296689L;
  
  // <model_name, data>
  protected Map<String, String> results = new HashMap<>();

  public MLEventIn() {}

  public MLEventIn(long id, long timestamp) {
    super(id, timestamp);
  }

  public MLEventIn(MLEventIn e) {
    id = e.getId();
    results = e.getResults();
    started = e.isStarted();
    timestamp = e.getTimestamp();
    startTimestamp = e.getStartTimestamp();
    finishTimestamp = e.getFinishTimestamp();
    data = e.getData();
  }

  public MLEventIn(MLEventOut e) {
    id = e.getId();
    started = e.isStarted();
    timestamp = e.getTimestamp();
    startTimestamp = e.getStartTimestamp();
    finishTimestamp = e.getFinishTimestamp();
    data = e.getData();
  }

  public MLEventIn dup() {
    var newEvent = dupWithNoData();
    newEvent.setData(data);

    return newEvent;
  }

  public MLEventIn dupWithNoData() {
    var newEvent = new MLEventIn();
    newEvent.setId(id);
    newEvent.setResults(new HashMap<>(results));
    newEvent.setStarted(started);
    newEvent.setTimestamp(timestamp);
    newEvent.setStartTimestamp(startTimestamp);
    newEvent.setFinishTimestamp(finishTimestamp);

    return newEvent;
  }

  public MLEventOut toMLEventOut() {
    return new MLEventOut(this);
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public INDArray getDataAsINDArray() {
    if (data == null)
      return null;
    
    return BinarySerde.toArray(ByteBuffer.wrap(data));
  }

  public void setDataFromINDArray(INDArray mat) {
    var buf = BinarySerde.toByteBuffer(mat);
    data = new byte[buf.remaining()];
    buf.get(data);
  }

  public Map<String, String> getResults() {
    return results;
  }

  public void putResult(String modelName, String result) {
    results.put(modelName, result);
  }

  public void setResults(Map<String, String> results) {
    this.results = results;
  }
}
