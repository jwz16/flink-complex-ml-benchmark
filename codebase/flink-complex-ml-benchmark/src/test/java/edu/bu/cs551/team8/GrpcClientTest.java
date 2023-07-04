package edu.bu.cs551.team8;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

// import java.util.List;
// import java.util.Vector;
// import edu.bu.cs551.team8.rpc.TorchServeGrpcClient;

public class GrpcClientTest {
  @Test
  public void testTorchServeGrpcClient() throws Exception {
    /**
     * Uncomment to test TorchServeGrpcClient, need to bring up TorchServe server first.
     */

    // var client  = new TorchServeGrpcClient("localhost", 7070);

    // String testImagePath = "test_data/1.jpeg";

    // List<String> testImagePaths = new Vector<>();
    // testImagePaths.add(testImagePath);
    // client.sendImages(testImagePaths, true, "mobilenet_v1");
    // client.close();

    assertTrue(true);
  }
}
