package edu.bu.flink_complex_ml_benchmark.connectors.events;

import com.google.gson.JsonObject;

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

  @Override
  public String serialize() {
    var jsonObj =  super.serializeToJsonObject();

    jsonObj.addProperty("result", result);

    return jsonObj.toString();
  }

  @Override
  public JsonObject deserialize(String value) {
    var jsonObj = super.deserialize(value);

    result = jsonObj.get("result").getAsString();

    return jsonObj;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

}
