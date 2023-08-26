package edu.bu.flink_complex_ml_benchmark.pipelines;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import edu.bu.flink_complex_ml_benchmark.ComplexMLBenchmark;
import edu.bu.flink_complex_ml_benchmark.Config;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;
import edu.bu.flink_complex_ml_benchmark.rpc.TorchServeGrpcClient;

public class TorchServePipeline extends ExternalPipeline {
  private static final int ASYNC_OPERATOR_CAPACITY = 10000;
  private static final long ASYNC_OPERATOR_TIMEOUT = 10000;

  public TorchServePipeline(ComplexMLBenchmark.Type benchmarkType, Pipeline p) {
    super(p);
    
    this.benchmarkType = benchmarkType;
    this.type = Type.TORCH_SERVE;
  }

  @Override
  public void run() throws Exception {
    setupStream();

    resultStream.sinkTo(sink);

    // Execute the program
    env.execute("Executing " + this.toString());

    TorchServeGrpcClient.getInstance().close();
  }

  @Override
  protected DataStream<MLEvent> modelStreamOp(ModelNode node, Set<DataStream<MLEvent>> attachedInputStreams) {
    var config  = Config.getInstance();
    DataStream<MLEvent> op = null;
    if (config.isForceSyncRequest()) {
      op = mergeStreams(attachedInputStreams).process(node.getHandler().toSyncFunction());
    } else {
      op = AsyncDataStream.orderedWait(
        mergeStreams(attachedInputStreams),
        node.getHandler().toAsyncFunction(),
        ASYNC_OPERATOR_TIMEOUT,
        TimeUnit.MILLISECONDS,
        ASYNC_OPERATOR_CAPACITY
      );
    }

    return op;
  }

  @Override
  public String toString() {
    return "TorchServePipeline";
  }
  
}
