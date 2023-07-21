package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class MLEventOutSchema implements DeserializationSchema<MLEventOut>, SerializationSchema<MLEventOut> {

  @Override
  public TypeInformation<MLEventOut> getProducedType() {
    return TypeInformation.of(MLEventOut.class);
  }

  @Override
  public byte[] serialize(MLEventOut e) {
    return e.serialize().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public MLEventOut deserialize(byte[] message) throws IOException {
    var e = new MLEventOut();
    e.deserialize(new String(message, StandardCharsets.UTF_8));

    return e;
  }

  @Override
  public boolean isEndOfStream(MLEventOut nextElement) {
    return false;
  }
  
}
