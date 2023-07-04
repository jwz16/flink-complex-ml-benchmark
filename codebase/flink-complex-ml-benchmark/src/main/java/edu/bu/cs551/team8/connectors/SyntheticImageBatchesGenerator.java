package edu.bu.cs551.team8.connectors;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.binary.BinarySerde;

import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLSyntheticImageBatchEvent;

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
    Nd4j.getRandom().setSeed(eventId);
    var mat = Nd4j.rand(batchSize, 3, imageSize, imageSize).muli(255).castTo(DataType.UINT8);
    var buf = BinarySerde.toByteBuffer(mat);
    var arr = new byte[buf.remaining()];
    buf.get(arr);

    return new MLSyntheticImageBatchEvent(eventId++, System.nanoTime(), arr);
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
