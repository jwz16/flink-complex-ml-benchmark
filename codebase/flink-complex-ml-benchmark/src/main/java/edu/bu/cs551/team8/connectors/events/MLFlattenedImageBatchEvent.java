package edu.bu.cs551.team8.connectors.events;
import edu.bu.cs551.team8.types.FlattenedImageBatch;

public class MLFlattenedImageBatchEvent extends MLEventIn {

  private static final long serialVersionUID = 2384244530029634782L;
  private FlattenedImageBatch batch;

  public MLFlattenedImageBatchEvent() {}
  
  public MLFlattenedImageBatchEvent(long id, long timestamp, FlattenedImageBatch batch) {
    super(id, timestamp);

    this.batch = batch;
  }

  @Override
  public String toString() {
    return String.format("MLImagesBatchEvent{timestamp=%d, batch size=%d, image size=%d}", timestamp, batch.size(), batch.getData().get(0).size());
  }

  public FlattenedImageBatch getBatch() {
    return batch;
  }

  public void setBatch(FlattenedImageBatch batch) {
    this.batch = batch;
  }
  
}
