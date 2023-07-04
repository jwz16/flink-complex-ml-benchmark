package edu.bu.cs551.team8.pipelines;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import edu.bu.cs551.team8.ComplexMLBenchmark;
import edu.bu.cs551.team8.Config;
import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLEventOut;
import edu.bu.cs551.team8.pipelines.nodes.ModelNode;
import edu.bu.cs551.team8.rpc.TorchServeGrpcClient;

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
  protected DataStream<MLEventOut> modelStreamOp(ModelNode node, Set<DataStream<MLEventIn>> attachedInputStreams) {
    var config  = Config.getInstance();
    DataStream<MLEventOut> op = null;
    if (config.isForceSyncRequest()) {
      op = connectStreams(attachedInputStreams).process(node.getHandler().toSyncFunction());
    } else {
      op = AsyncDataStream.orderedWait(
        connectStreams(attachedInputStreams),
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
