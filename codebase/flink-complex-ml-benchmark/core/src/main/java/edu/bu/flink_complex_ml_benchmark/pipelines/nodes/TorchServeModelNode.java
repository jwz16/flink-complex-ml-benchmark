package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import java.util.Base64;

import org.nd4j.linalg.factory.Nd4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventIn;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventOut;
import edu.bu.flink_complex_ml_benchmark.rpc.TorchServeGrpcClient;

public class TorchServeModelNode extends ExternalModelNode {

  public TorchServeModelNode(PipelineNode node) {
    super(node);
  }

  private static final long serialVersionUID = -8199587916118485070L;

  @Override
  public void open() { /* DO NOTHING */ }

  @Override
  public void close() { /* DO NOTHING */ }

  /**
   * TorchServe model process function
   * @param input
   */
  @Override
  public MLEventOut process(MLEventIn input) {
    super.process(input);

    if (input.getDataAsINDArray() == null) {
      return input.toMLEventOut();
    }
    
    var inputMat = input.getDataAsINDArray();
    var b64Data = Base64.getEncoder().encodeToString(Nd4j.toNpyByteArray(inputMat));

    var jsonObj = new JsonObject();
    jsonObj.addProperty("data", b64Data);

    var shape = new JsonArray();
    for (var s : inputMat.shape()) {
      shape.add(s);
    }
    jsonObj.add("shape", shape);
    
    var dataToSend = ByteString.copyFrom(jsonObj.toString().getBytes());

    var eventOut = input.toMLEventOut();
    
    try {
      String result = TorchServeGrpcClient.getInstance().sendData(dataToSend, this.name);
      eventOut.setResult(result);
      eventOut.setData(input.getData());
    } catch (Exception e) {
      e.printStackTrace();

      eventOut.setResult(null);
      eventOut.setData(null);
    }

    return eventOut;
  }
  
}
