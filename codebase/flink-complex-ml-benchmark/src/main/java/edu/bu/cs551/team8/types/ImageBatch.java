package edu.bu.cs551.team8.types;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.serde.binary.BinarySerde;

public class ImageBatch implements Serializable {

  private static final long serialVersionUID = -7876240484437685326L;

  // private INDArray data = null;
  private byte[] data = null;

  public ImageBatch() {}

  public ImageBatch(byte[] data) {
    this.data = data;
  }

  public ImageBatch dup() {
    return new ImageBatch(data.clone());
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public INDArray getDataAsINDArray() {
    if (data == null)
      return null;
    
    return BinarySerde.toArray(ByteBuffer.wrap(data));
  }

  public void setDataFromINDArray(INDArray mat) {
    var buf = BinarySerde.toByteBuffer(mat);
    data = new byte[buf.remaining()];
    buf.get(data);
  }

}
