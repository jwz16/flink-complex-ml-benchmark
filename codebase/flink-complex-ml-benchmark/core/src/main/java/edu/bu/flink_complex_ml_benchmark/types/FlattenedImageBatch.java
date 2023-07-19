package edu.bu.flink_complex_ml_benchmark.types;

import java.io.Serializable;
import java.util.List;

public class FlattenedImageBatch implements Serializable {

  private static final long serialVersionUID = 7041877361170432918L;
  
  private List<FlattenedImage> data;

  public FlattenedImageBatch() {}

  public FlattenedImageBatch(List<FlattenedImage> data) {
    this.data = data;
  }

  public List<FlattenedImage> getData() {
    return data;
  }

  public void setData(List<FlattenedImage> data) {
    this.data = data;
  }
  
  public int size() {
    return this.data.size();
  }

}
