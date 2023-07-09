package edu.bu.flink_complex_ml_benchmark.rpc;

import java.io.IOException;
import java.util.List;

import com.google.protobuf.ByteString;

public interface GrpcClient {
  String sendImages(List<String> filePaths, Boolean isResourceFile, String modelName) throws IOException;
  String sendData(ByteString payload, String modelName) throws IOException;
  
  void close();
}
