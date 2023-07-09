package edu.bu.flink_complex_ml_benchmark.connectors.events;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.ops.NDBase;
import org.nd4j.serde.binary.BinarySerde;

import edu.bu.flink_complex_ml_benchmark.types.ImageBatch;

public class MLImageBatchEvent extends MLImageFileBatchEvent {

  private static final long serialVersionUID = 3132430024313511546L;

  private ImageBatch imgBatch = new ImageBatch();
  private static NativeImageLoader imgLoader = new NativeImageLoader();;
  
  public MLImageBatchEvent() {
  }
  
  /**
   * 
   * @param id
   * @param timestamp
   * @param batch, list of image path
   */
  public MLImageBatchEvent(long id, long timestamp, List<String> batch) {
    super(id, timestamp, batch);

    loadImages();
    setData(imgBatch.getData());
  }

  public MLImageBatchEvent(MLEventIn e) {
    super(e);
  }
  
  @Override
  public MLImageBatchEvent dup() {
    var e = new MLImageBatchEvent(super.dupWithNoData());

    e.setImgBatch(imgBatch.dup());
    e.setData(imgBatch.getData());
    return e;
  }

  private void loadImages() {
    var ndbase = new NDBase();
    for (String path : imgFilePathBatch) {
      InputStream is = null;
      if (isResourceFile) {
        is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
          return;
        }
      } else {
        try {
          is = new FileInputStream(path);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          return;
        }
      }
      
      try {
        var inputMat = imgLoader.asMatrix(is).castTo(DataType.UINT8); // 4 dimensions, nchw

        if (imgBatch.getData() == null) {
          var buf = BinarySerde.toByteBuffer(inputMat);
          var arr = new byte[buf.remaining()];
          buf.get(arr);
          
          imgBatch.setData(arr);
        } else {
          // add the new image to the image batch.
          var mat = imgBatch.getDataAsINDArray();
          var newMat = ndbase.concat(0, mat, inputMat);
          imgBatch.setDataFromINDArray(newMat);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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
