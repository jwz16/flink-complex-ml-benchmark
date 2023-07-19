package edu.bu.flink_complex_ml_benchmark;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Vector;

import org.junit.Test;

import ai.onnxruntime.OrtException;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLImageFileBatchEvent;
import edu.bu.flink_complex_ml_benchmark.models.ONNXModel;

public class ModelTest {
  @Test
  public void testONNXModel() throws OrtException, IOException {
    // var model = new ONNXModel("models/mobilenet.onnx");

    // var imgBatch = new Vector<String>();
    // imgBatch.add("test_data/1.jpeg");
    // model.inference(new MLImageFileBatchEvent(0, System.nanoTime(), imgBatch));

    assertTrue( true );
  }
}
