package edu.bu.flink_complex_ml_benchmark.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHelper {

  public static Logger logger = LoggerFactory.getLogger(HttpHelper.class);

  public static String sendImages(List<String> filePaths, String uri, Boolean isResourceFile) throws Exception {
    try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
      final HttpPost httppost = new HttpPost(uri);
      
      MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
      for (String path : filePaths) {
        if (isResourceFile) {
          InputStreamBody bin = new InputStreamBody(HttpHelper.class.getResourceAsStream(path), path);
          entityBuilder.addPart("data", bin);
        } else {
          FileBody bin = new FileBody(new File(path));
          entityBuilder.addPart("data", bin);
        }

      }

      final HttpEntity reqEntity = entityBuilder.build();

      httppost.setEntity(reqEntity);

      HttpHelper.logger.info("executing request " + httppost);
      return httpclient.execute(httppost, response -> {
        final HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            // Uses a buffered reader to parse the input stream
            BufferedReader in = new BufferedReader(new InputStreamReader(resEntity.getContent()));
            StringBuffer rspContent = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
              rspContent.append(inputLine);
            }
            in.close();

            return rspContent.toString();
        }

        return null;
      });
    }
  }

}
