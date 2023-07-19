package edu.bu.flink_complex_ml_benchmark;

import java.lang.reflect.InvocationTargetException;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.pytorch.serve.grpc.inference.PredictionResponse;
import org.pytorch.serve.grpc.inference.PredictionsRequest;

import com.twitter.chill.protobuf.ProtobufSerializer;

import edu.bu.flink_complex_ml_benchmark.handlers.BaseModelHandler;

public class Util {
  public static void registerProtobufType(StreamExecutionEnvironment env) {
    var config = env.getConfig();
    config.registerTypeWithKryoSerializer(PredictionsRequest.class, ProtobufSerializer.class);
    config.registerTypeWithKryoSerializer(PredictionResponse.class, ProtobufSerializer.class);
  }

  public static BaseModelHandler experimentsHandler(String pipelineId, String handlerClass) {
    if (handlerClass == null)
      return null;
    
    String handlerClassFull = String.format("edu.bu.flink_complex_ml_benchmark.experiments.handlers.%s.%s", pipelineId, handlerClass);
    try {
      var clazz = Class.forName(handlerClassFull);
      var constructor = clazz.getConstructor();
      BaseModelHandler handler = (BaseModelHandler) constructor.newInstance();
      return handler;
    } catch (
      ClassNotFoundException | 
      NoSuchMethodException | 
      SecurityException | 
      InstantiationException | 
      IllegalAccessException | 
      IllegalArgumentException | 
      InvocationTargetException e
    ) {
      e.printStackTrace();
    }

    return null;
  }
}
