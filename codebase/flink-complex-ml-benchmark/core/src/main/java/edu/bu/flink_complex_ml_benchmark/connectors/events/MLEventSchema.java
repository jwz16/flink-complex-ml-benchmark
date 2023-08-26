package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class MLEventSchema implements DeserializationSchema<MLEvent>, SerializationSchema<MLEvent> {

  @Override
  public TypeInformation<MLEvent> getProducedType() {
    return TypeInformation.of(MLEvent.class);
  }

  @Override
  public byte[] serialize(MLEvent e) {
    return e.serialize().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public MLEvent deserialize(byte[] message) throws IOException {
    var e = new MLEvent();
    e.deserialize(new String(message, StandardCharsets.UTF_8));
    return e;
  }

  @Override
  public boolean isEndOfStream(MLEvent nextElement) {
    return false;
  }
  
}
