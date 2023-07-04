package edu.bu.cs551.team8.connectors;

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.examples.utils.ThrottledIterator;

import edu.bu.cs551.team8.connectors.events.MLEventIn;

public class MLRepeatedImageBatchesSource extends RichParallelSourceFunction<MLEventIn> {

  private static final long serialVersionUID = 8106084801020963866L;
  protected volatile boolean isRunning = true;
  private Generator generator;
  private ThrottledIterator<MLEventIn> throttledIterator;
  private boolean shouldBeThrottled = false;

  public MLRepeatedImageBatchesSource(String imageFilePath, int batchSize, int experimentTimeInSeconds, int warmupRequestsNum, int inputRate) {
    this.generator = new RepeatedImageBatchesGenerator(imageFilePath, batchSize, experimentTimeInSeconds, warmupRequestsNum);

    // An input rate equal to 0 means that the source should not be throttled
    if (inputRate > 0) {
      this.shouldBeThrottled = true;
      this.throttledIterator = new ThrottledIterator<MLEventIn>(generator, inputRate / 2);
    }
  }

  @Override
  public void run(SourceContext<MLEventIn> ctx) throws Exception {
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
