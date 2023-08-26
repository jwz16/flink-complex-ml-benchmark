package edu.bu.flink_complex_ml_benchmark.connectors;

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.examples.utils.ThrottledIterator;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;

public class MLSyntheticImageBatchesSource extends RichParallelSourceFunction<MLEvent> {
  private static final long serialVersionUID = -8729658830449241210L;
  protected volatile boolean isRunning = true;
  private Generator generator;
  private ThrottledIterator<MLEvent> throttledIterator;
  private boolean shouldBeThrottled = false;
  
  public MLSyntheticImageBatchesSource(int imageSize, int batchSize, int experimentTimeInSeconds, int warmupRequestsNum, int inputRate) {
    this.generator = new SyntheticImageBatchesGenerator(imageSize, batchSize, experimentTimeInSeconds, warmupRequestsNum);
    
    // An input rate equal to 0 means that the source should not be throttled
    if (inputRate > 0) {
      this.shouldBeThrottled = true;
      this.throttledIterator = new ThrottledIterator<MLEvent>(generator, inputRate / 2);
    }
  }

  @Override
  public void run(SourceContext<MLEvent> ctx) throws Exception {
    this.isRunning = true;
    if (this.shouldBeThrottled) {
      while (isRunning && this.throttledIterator.hasNext()) {
        ctx.collect(this.throttledIterator.next());
      }
    } else {
      while (isRunning && this.generator.hasNext()) {
        ctx.collect(this.generator.next());
      }
    }
  }

  @Override
  public void cancel() {
    this.isRunning = false;
  }

  public Generator getGenerator() {
    return generator;
  }

  public void setGenerator(Generator generator) {
    this.generator = generator;
  }
  
}
