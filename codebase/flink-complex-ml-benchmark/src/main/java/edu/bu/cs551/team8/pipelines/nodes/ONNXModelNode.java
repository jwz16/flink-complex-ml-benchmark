package edu.bu.cs551.team8.pipelines.nodes;

import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLEventOut;
import edu.bu.cs551.team8.models.ONNXModel;

public class ONNXModelNode extends EmbeddedModelNode {

  private static final long serialVersionUID = 7020531963073250358L;
  
  private ONNXModel model = null;

  public ONNXModelNode(PipelineNode node) {
    super(node);
  }

  /**
   * ONNX model process function
   * @param input
   */
  @Override
  public MLEventOut process(MLEventIn input) {
    var eventOut = new MLEventOut();
    try {
      // TODO

      // String result = this.model.inference(input.getData().get(this.name).get(0));
      // eventOut.setData(Arrays.asList(result.getBytes()));
    } catch (Exception e) {
      e.printStackTrace();

      eventOut.setResult(null);
      eventOut.setData(null);
    }

    return eventOut;
  }

  public ONNXModel getModel() {
    return model;
  }

  public void setModel(ONNXModel model) {
    this.model = model;
  }

}
