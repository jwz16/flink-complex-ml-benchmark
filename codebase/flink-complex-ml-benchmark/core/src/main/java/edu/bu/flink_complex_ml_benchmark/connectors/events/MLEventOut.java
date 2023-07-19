package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.nio.ByteBuffer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.serde.binary.BinarySerde;

public class MLEventOut extends MLEvent {
  
  private static final long serialVersionUID = 2399269973720482248L;

  protected String result;

  public MLEventOut() {}

  public MLEventOut(MLEventIn e) {
    id = e.getId();
    timestamp = e.getTimestamp();
    startTimestamp = e.getStartTimestamp();
    finishTimestamp = e.getFinishTimestamp();
    started = e.isStarted();
    data = e.getData();
  }

  public MLEventIn toMLEventIn() {
    return new MLEventIn(this);
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

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

}
