package edu.bu.cs551.team8;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.bu.cs551.team8.pipelines.Pipeline;
import edu.bu.cs551.team8.pipelines.nodes.PipelineNode;

public class PipelineTest {
  @Test
  public void testPipelineLoadFromYaml() {
    Pipeline p = Pipeline.build("test_pipelines/pipeline-sample.yaml", Pipeline.Type.TORCH_SERVE);
    assertEquals(p.getName(), "Pipeline 1");

    List<PipelineNode> nodes = p.getNodes();
    assertEquals(nodes.get(0).getId(), Integer.valueOf(1));
    assertEquals(nodes.get(0).getType(), "preprocessor");

    assertEquals(nodes.get(1).getId(), Integer.valueOf(2));
    assertEquals(nodes.get(1).getType(), "model");

    assertEquals(nodes.get(2).getId(), Integer.valueOf(3));
    assertEquals(nodes.get(2).getType(), "model");
  }

  @Test
  public void testPipelineInitialize() {
    Pipeline p = Pipeline.build("test_pipelines/pipeline-sample.yaml", Pipeline.Type.TORCH_SERVE);
    assertNotEquals(p.getGraph(), null);
  }

  @Test
  public void testInvalidPipelineShouldFail() {
    Pipeline p = Pipeline.build("test_pipelines/pipeline-invalid.yaml", Pipeline.Type.TORCH_SERVE);
    assertEquals(p.getGraph(), null);
  }

  @Test
  public void testLayeredNodes() {
    Pipeline p = Pipeline.build("test_pipelines/pipeline-complex-sample.yaml", Pipeline.Type.TORCH_SERVE);
    
    List<Set<Integer>> nodesIdx = new ArrayList<>();
    nodesIdx.add(new HashSet<>(Arrays.asList(1)));
    nodesIdx.add(new HashSet<>(Arrays.asList(2, 3)));
    nodesIdx.add(new HashSet<>(Arrays.asList(4)));
    
    var layers = p.getLayeredNodes();
    for (int i = 0; i < layers.size(); i++) {
      var nodes = layers.get(i);
      var indices = nodesIdx.get(i);

      for (var node : nodes) {
        assertTrue(indices.contains(node.getId()));
      }
    }
  }

  @Test
  public void testPipelineOutputInputEqualSize() throws Exception {
    /* run TorchServe before running this unit test case*/

    Config.getInstance().setExperimentTimeInSeconds(5);
    Config.getInstance().setWarmupRequestsNumber(0);
    Config.getInstance().setImageSize(32);

    Pipeline p = Pipeline.build(ComplexMLBenchmark.Type.EXTERNAL, Pipeline.Type.TORCH_SERVE, "test_pipelines/pipeline4.yaml");
    p.setupStream();
    var sink = new DummySink<String>();
    p.getResultStream().addSink(sink);
    p.getEnv().execute("testPipelineOutputInputEqualSize");
    assertEquals(p.getGenerator().getEventId(), sink.getRecords().size());
  }
}
