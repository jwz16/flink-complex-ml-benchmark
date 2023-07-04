package edu.bu.cs551.team8;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

// import java.util.List;
// import java.util.Vector;

// import edu.bu.cs551.team8.http.HttpHelper;
// import edu.bu.cs551.team8.pipelines.TorchServePipeline;

public class HttpHelperTest {
  @Test
  public void testSendImages() throws Exception {

    assertTrue(true);
    
    /**
     * To run this test case, we need to run torchserve first,
     * after running the torchserve, simply remove the above line and
     * uncomment the following lines and the above imports, repace the testImagePath to a concrete image path.
     * Then run this teat case.
     */
    
    // String endpoint = TorchServePipeline.torchServeWorkflowEndpoint();
    // String testImagePath = "test_data/1.jpeg"

    // List<String> testImagePaths = new Vector<>();
    // testImagePaths.add(testImagePath);
    // String rsp = HttpHelper.sendImages(testImagePaths, endpoint, true);
    // assertTrue(rsp.length() > 0);

  }
}
