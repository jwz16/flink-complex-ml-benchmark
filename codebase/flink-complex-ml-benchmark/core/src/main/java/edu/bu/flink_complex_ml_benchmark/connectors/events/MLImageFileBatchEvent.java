package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.util.List;

public class MLImageFileBatchEvent extends MLEvent {

  private static final long serialVersionUID = -4873262489030230386L;
  
  protected List<String> imgFilePathBatch; // image file paths as a batch
  protected Boolean isResourceFile = true;

  public MLImageFileBatchEvent() {}
  
  public MLImageFileBatchEvent(long id, long timestamp, List<String> batch) {
    super(id, timestamp);

    this.imgFilePathBatch = batch;
  }

  public MLImageFileBatchEvent(MLEvent e) {
    super(e);
  }

  @Override
  public String toString() {
    return String.format("MLImageFileBatchEvent{timestamp=%d, batch size=%d}", timestamp, imgFilePathBatch.size());
  }

  public List<String> getBatch() {
    return imgFilePathBatch;
  }

  public List<String> getImgFilePathBatch() {
    return imgFilePathBatch;
  }

  public void setImgFilePathBatch(List<String> imgFilePathBatch) {
    this.imgFilePathBatch = imgFilePathBatch;
  }

  public Boolean getIsResourceFile() {
    return isResourceFile;
  }

  public void setIsResourceFile(Boolean isResourceFile) {
    this.isResourceFile = isResourceFile;
  }
  
}
