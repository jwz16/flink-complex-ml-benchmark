package edu.bu.flink_complex_ml_benchmark.connectors;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLSyntheticImageBatchEvent;

public class SyntheticImageBatchesGenerator extends Generator {

  private static final long serialVersionUID = 4268683604623221824L;

  private static long eventId = 0;

  private final int imageSize;
  private final int batchSize;
  private int experimentTime;
  private int warmupRequestsNum;
  private boolean finishedWarmUp;
  private long startTime;

  public SyntheticImageBatchesGenerator(int imageSize, int batchSize, int experimentTimeInSeconds, int warmupRequestsNum) {
    this.batchSize = batchSize;
    this.imageSize = imageSize;
    this.experimentTime = experimentTimeInSeconds * 1000;
    this.warmupRequestsNum = warmupRequestsNum;

    this.finishedWarmUp = false;
  }
  
  /**
   * Check if the generator has next event to generate.
   * Depends on if the total experiment time is bigger than experimentTime
   * @return  false means experiment ends, otherwise, true means experiment keeps going.
   */
  @Override
  public boolean hasNext() {
    if (finishedWarmUp) {
      if (System.currentTimeMillis() - startTime > experimentTime)
        return false;
    } else {
      if (--warmupRequestsNum < 0) {
        finishedWarmUp = true;
        this.startTime = System.currentTimeMillis();
      }
    }
    return true;
  }
  
  /**
   * Generate MLSyntheticImageBatchEvent, which extends MLEvent.
   * @return a new MLSyntheticImageBatchEvent
   */
  @Override
  public MLEvent next() {
    Nd4j.getRandom().setSeed(eventId);
    var mat = Nd4j.rand(batchSize, 3, imageSize, imageSize).muli(255).castTo(DataType.UINT8);
    var e = new MLSyntheticImageBatchEvent(eventId++, System.nanoTime(), null);
    e.setDataFromINDArray(mat);
    return e;
  }

  @Override
  public long getEventId() {
    return SyntheticImageBatchesGenerator.eventId;
  }

  @Override
  public void setEventId(long eventId) {
    SyntheticImageBatchesGenerator.eventId = eventId;
  }
  
}
