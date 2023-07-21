package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class MLEventInSchema implements DeserializationSchema<MLEventIn>, SerializationSchema<MLEventIn> {

  @Override
  public TypeInformation<MLEventIn> getProducedType() {
    return TypeInformation.of(MLEventIn.class);
  }

  @Override
  public byte[] serialize(MLEventIn e) {
    return e.serialize().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public MLEventIn deserialize(byte[] message) throws IOException {
    var e = new MLEventIn();
    e.deserialize(new String(message, StandardCharsets.UTF_8));
    return e;
  }

  @Override
  public boolean isEndOfStream(MLEventIn nextElement) {
    return false;
  }
  
}
