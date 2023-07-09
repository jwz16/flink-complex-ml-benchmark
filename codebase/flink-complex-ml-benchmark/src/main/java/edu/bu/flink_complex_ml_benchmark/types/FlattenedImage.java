package edu.bu.flink_complex_ml_benchmark.types;

import java.io.Serializable;
import java.util.List;

public class FlattenedImage implements Serializable {
  
  private static final long serialVersionUID = -1339968616175161965L;
  private List<Float> data;

  public FlattenedImage() {}

  public FlattenedImage(List<Float> data) {
    this.data = data;
  }

  public List<Float> getData() {
    return data;
  }

  public void setData(List<Float> data) {
    this.data = data;
  }
  
  public int size() {
    return this.data.size();
  }

}
