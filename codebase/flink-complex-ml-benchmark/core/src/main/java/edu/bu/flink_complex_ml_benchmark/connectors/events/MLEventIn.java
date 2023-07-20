package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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

  @Override
  public String serialize() {
    var jsonObj = super.serializeToJsonObject();
    jsonObj.addProperty("results", new Gson().toJson(results));

    return jsonObj.toString();
  }

  @Override
  public JsonObject deserialize(String value) {
    var jsonObj = super.deserialize(value);

    Type type = new TypeToken<Map<String, String>>(){}.getType();
    results = new Gson().fromJson(jsonObj.get("results").getAsString(), type);

    return jsonObj;
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
