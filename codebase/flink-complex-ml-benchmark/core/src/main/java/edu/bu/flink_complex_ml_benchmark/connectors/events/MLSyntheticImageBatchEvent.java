package edu.bu.flink_complex_ml_benchmark.connectors.events;

import org.nd4j.linalg.api.ndarray.INDArray;

import edu.bu.flink_complex_ml_benchmark.types.ImageBatch;

public class MLSyntheticImageBatchEvent extends MLEvent {

  private static final long serialVersionUID = 3132430024313511546L;

  private ImageBatch imgBatch = new ImageBatch();
  
  public MLSyntheticImageBatchEvent() {
  }
  
  /**
   * 
   * @param id
   * @param timestamp
   * @param batch, list of image path
   */
  public MLSyntheticImageBatchEvent(long id, long timestamp, byte[] data) {
    super(id, timestamp);

    this.data = data;
    imgBatch.setData(data);
  }

  public MLSyntheticImageBatchEvent(MLEvent e) {
    super(e);
  }
  
  @Override
  public MLSyntheticImageBatchEvent dup() {
    var e = new MLSyntheticImageBatchEvent(super.dupWithNoData());

    e.setImgBatch(imgBatch.dup());
    e.setData(imgBatch.getData());
    return e;
  }

  public ImageBatch getImgBatch() {
    return imgBatch;
  }

  public void setImgBatch(ImageBatch imgBatch) {
    this.imgBatch = imgBatch;
  }

  @Override
  public byte[] getData() {
    return imgBatch.getData();
  }

  @Override
  public void setData(byte[] data) {
    imgBatch.setData(data);
  }

  @Override
  public INDArray getDataAsINDArray() {
    return imgBatch.getDataAsINDArray();
  }

  @Override
  public void setDataFromINDArray(INDArray mat) {
    imgBatch.setDataFromINDArray(mat);
  }
  
}
