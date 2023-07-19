package edu.bu.flink_complex_ml_benchmark.connectors;

import java.util.List;
import java.util.Vector;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLImageBatchEvent;

public class RepeatedImageBatchesGenerator extends Generator {

  private static final long serialVersionUID = 8888717715099129611L;

  private static long eventId = 0;
  private final String imageFilePath;
  private final int batchSize;
  private int experimentTime;
  private int warmupRequestsNum;
  private boolean finishedWarmUp;
  private long startTime;

  public RepeatedImageBatchesGenerator(String imageFilePath, int batchSize, int experimentTimeInSeconds, int warmupRequestsNum) {
    this.imageFilePath = imageFilePath;
    this.batchSize = batchSize;
    this.experimentTime = experimentTimeInSeconds * 1000;
    this.warmupRequestsNum = warmupRequestsNum;
    this.finishedWarmUp = false;
  }

  @Override
  public boolean hasNext() {
    if (!finishedWarmUp) {
      if (--warmupRequestsNum < 0) {
        finishedWarmUp = true;
        this.startTime = System.currentTimeMillis();
      }
    } else {
      if (System.currentTimeMillis() - startTime > experimentTime)
        return false;
    }
    return true;
  }

  @Override
  public MLEventIn next() {
    List<String> batchData = new Vector<>();
    for (int imgNum = 0; imgNum < this.batchSize; imgNum++) {
      batchData.add(imageFilePath);
    }
    return new MLImageBatchEvent(eventId++, System.nanoTime(), batchData);
  }

  @Override
  public long getEventId() {
    return RepeatedImageBatchesGenerator.eventId;
  }

  @Override
  public void setEventId(long eventId) {
    RepeatedImageBatchesGenerator.eventId = eventId;
  }
  
}
