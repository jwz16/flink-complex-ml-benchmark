package edu.bu.flink_complex_ml_benchmark.http;

import java.util.Collections;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import edu.bu.flink_complex_ml_benchmark.connectors.events.MLImageFileBatchEvent;

public class AsyncInferenceRequest extends RichAsyncFunction<MLImageFileBatchEvent, Tuple3<String, Long, Long>> {
  private static final long serialVersionUID = -1893082030332560931L;
  private final String url;

  public AsyncInferenceRequest(String url) throws Exception {
      // Create a neat value object to hold the URL
      this.url = url;
  }

  @Override
  public void asyncInvoke(MLImageFileBatchEvent inputEvent,
                          ResultFuture<Tuple3<String, Long, Long>> resultFuture) throws Exception {
      long startTime = System.nanoTime();
      // Append all the edges to a string
      
      String result = HttpHelper.sendImages(inputEvent.getBatch(), this.url, true);
      // NOTE: Measures the duration for the whole batch
      resultFuture.complete(Collections.singleton(new Tuple3<>(result, startTime, System.nanoTime())));
  }
}