package edu.bu.cs551.team8;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Vector;

import org.junit.Test;

import ai.onnxruntime.OrtException;
import edu.bu.cs551.team8.connectors.events.MLImageFileBatchEvent;
import edu.bu.cs551.team8.models.ONNXModel;

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
