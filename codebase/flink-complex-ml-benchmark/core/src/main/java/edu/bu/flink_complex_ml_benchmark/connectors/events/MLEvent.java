package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Base64;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.binary.BinarySerde;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

  public String serialize() {
    return serializeToJsonObject().toString();
  }

  protected JsonObject serializeToJsonObject() {
    var inputMat = getDataAsINDArray();
    var b64Data = Base64.getEncoder().encodeToString(Nd4j.toNpyByteArray(inputMat));

    var jsonObj = new JsonObject();
    jsonObj.addProperty("data", b64Data);

    var shape = new JsonArray();
    for (var s : inputMat.shape()) {
      shape.add(s);
    }
    jsonObj.add("shape", shape);

    jsonObj.addProperty("id", id);
    jsonObj.addProperty("started", started);
    jsonObj.addProperty("timestamp", timestamp);
    jsonObj.addProperty("start_timestamp", startTimestamp);
    jsonObj.addProperty("finish_timestamp", finishTimestamp);

    return jsonObj;
  }

  public JsonObject deserialize(String value) {
    var jsonElement = new Gson().fromJson(value, JsonElement.class);
    var jsonObj = jsonElement.getAsJsonObject();
    id = jsonObj.get("id").getAsLong();
    started = jsonObj.get("started").getAsBoolean();
    timestamp = jsonObj.get("timestamp").getAsLong();
    startTimestamp = jsonObj.get("start_timestamp").getAsLong();
    finishTimestamp = jsonObj.get("finish_timestamp").getAsLong();
    data = Base64.getDecoder().decode(
      jsonObj.get("data").getAsString()
    );

    setDataFromINDArray(Nd4j.createNpyFromByteArray(data));

    return jsonObj;
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
  
}
