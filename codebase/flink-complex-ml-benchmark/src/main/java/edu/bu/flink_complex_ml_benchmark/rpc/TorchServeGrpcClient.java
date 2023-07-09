package edu.bu.flink_complex_ml_benchmark.rpc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.pytorch.serve.grpc.inference.InferenceAPIsServiceGrpc;
import org.pytorch.serve.grpc.inference.PredictionsRequest;
import org.pytorch.serve.grpc.inference.InferenceAPIsServiceGrpc.InferenceAPIsServiceBlockingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import edu.bu.flink_complex_ml_benchmark.Config;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;;

public class TorchServeGrpcClient implements GrpcClient {
  protected static Logger logger = LoggerFactory.getLogger(TorchServeGrpcClient.class);
  
  private final InferenceAPIsServiceBlockingStub blockingStub;

  private static GrpcClient gInstance = null;

  public static GrpcClient getInstance() {
    if(gInstance == null) {
      var config = Config.getInstance();
      gInstance = new TorchServeGrpcClient(config.getTorchServeHost(), 7070);
    }

    return gInstance;
  }

  public TorchServeGrpcClient(String host, int port) {
    var channel = Grpc.newChannelBuilder(String.format("%s:%d", host, port), InsecureChannelCredentials.create())
        .maxInboundMessageSize(6553500)
        .build();

    blockingStub = InferenceAPIsServiceGrpc.newBlockingStub(channel);
  }

  public TorchServeGrpcClient(Channel channel) {
    blockingStub = InferenceAPIsServiceGrpc.newBlockingStub(channel);
  }

  @Override
  public String sendImages(List<String> filePaths, Boolean isResourceFile, String modelName) throws IOException {
    
    ByteString payload = null;
    for (String path : filePaths) {
      InputStream is = null;
      if (isResourceFile) {
        is = TorchServeGrpcClient.class.getClassLoader().getResourceAsStream(path);
      } else {
        is = new FileInputStream(path);
      }
      
      // TODO: need to support batch images
      payload = ByteString.readFrom(is);
    }

    return sendData(payload, modelName);
  }

  @Override
  public String sendData(ByteString payload, String modelName) throws IOException {
    var req = PredictionsRequest.newBuilder()
                                .setModelName(modelName)
                                .putInput("data",  payload)
                                .build();

    var rsp = blockingStub.predictions(req);
    return rsp.getPrediction().toStringUtf8();
  }

  @Override
  public void close() {
    ((ManagedChannel)blockingStub.getChannel()).shutdown();
  }

  public InferenceAPIsServiceBlockingStub getBlockingStub() {
    return blockingStub;
  }

}
