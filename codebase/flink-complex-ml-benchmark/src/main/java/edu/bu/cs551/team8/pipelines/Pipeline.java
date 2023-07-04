package edu.bu.cs551.team8.pipelines;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

import edu.bu.cs551.team8.ComplexMLBenchmark;
import edu.bu.cs551.team8.Config;
import edu.bu.cs551.team8.Util;
import edu.bu.cs551.team8.connectors.Generator;
import edu.bu.cs551.team8.connectors.MLSyntheticImageBatchesSource;
import edu.bu.cs551.team8.connectors.events.MLEventIn;
import edu.bu.cs551.team8.connectors.events.MLEventOut;
import edu.bu.cs551.team8.exceptions.UnknownPipelineType;
import edu.bu.cs551.team8.pipelines.nodes.ModelNode;
import edu.bu.cs551.team8.pipelines.nodes.PipelineNode;
import edu.bu.cs551.team8.pipelines.nodes.PreprocessorNode;
import edu.bu.cs551.team8.pipelines.nodes.TorchServeModelNode;

public class Pipeline {
  protected static Logger logger = LoggerFactory.getLogger(Pipeline.class);

  public enum Type {
    TORCH_SERVE, TF_SERVE, ONNX, ND4J, TF_SAVED_MODEL
  }

  protected StreamExecutionEnvironment env;
  protected DataStream<String> resultStream;
  protected FileSink<String> sink;
  protected Generator generator;

  protected Pipeline.Type type;
  protected ComplexMLBenchmark.Type benchmarkType;
  protected String name = "Unknown Pipeline";
  protected String id = "";
  protected String configPath;
  protected List<PipelineNode> nodes;
  protected List<Set<PipelineNode>> layeredNodes;
  protected MutableGraph<PipelineNode> graph = null;
  protected Boolean initialized = false;

  public static Pipeline build(
    ComplexMLBenchmark.Type benchmarkType,
    Pipeline.Type pipelineType,
    String configPath
  ) {
    switch (benchmarkType) {
      case EXTERNAL:
        return ExternalPipeline.build(benchmarkType, pipelineType, configPath);
      case EMBEDDED:
        return EmbeddedPipeline.build(benchmarkType, pipelineType, configPath);
      default:
        break;
    }

    return null;
  }

  public static Pipeline build(String configPath, Pipeline.Type pipelineType) {
    logger.info("building pipeline graph");

    try (InputStream input = Pipeline.class.getClassLoader().getResourceAsStream(configPath);) {
      Yaml yaml = new Yaml(Pipeline.buildPipelineYamlConstructor());

      Pipeline p = yaml.load(input);
      p.init(pipelineType);
      return p;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
  
  public Pipeline() {}

  /**
   * Copy constructor
   * @param p, another pipeline
   */
  public Pipeline(Pipeline p) {
    this.id = p.getId();
    this.name = p.getName();
    this.configPath = p.getConfigPath();
    this.nodes = p.getNodes();
    this.layeredNodes = p.getLayeredNodes();
    this.graph = p.getGraph();
  }

  public void setupStream() {
    var config = Config.getInstance();

    // Set up the streaming execution environment
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    Util.registerProtobufType(env);

    int inputPotentialProducers = (int) Math.ceil((double) config.getInputRate() / config.getMaxInputRatePerThread());
    int inputProducers = inputPotentialProducers > 0 ? inputPotentialProducers : 1;
    int inputRatePerProducer = Math.min(config.getInputRate(), config.getInputRate() / inputProducers);

    env.setParallelism(inputProducers);

    var inputSrc = new MLSyntheticImageBatchesSource(
      config.getImageSize(),
      config.getBatchSize(),
      config.getExperimentTimeInSeconds(),
      config.getWarmupRequestsNumber(),
      inputRatePerProducer
    );

    this.generator = inputSrc.getGenerator();

    DataStream<MLEventIn> inputStream = env.addSource(inputSrc);
    
    // build DAG datastream flow
    var allResults = buildDAG(inputStream);

    // Benchmarking - record timestamp when the scoring is done
    var results = mergeResults(allResults);

    DataStream<Tuple2<Long, Long>> records = results.map(new MapFunction<MLEventOut, Tuple2<Long, Long>>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Tuple2<Long, Long> map(MLEventOut e) throws Exception {
        // filter out failed requests
        if (e != null && e.getData() != null) {
          return new Tuple2<>(e.getStartTimestamp(), e.getFinishTimestamp());
        } else {
          return new Tuple2<>(-1L, -1L);
        }
      }
    });

    resultStream = records.map(new MapFunction<Tuple2<Long, Long>, String>() {
      private static final long serialVersionUID = 1L;

      @Override
      public String map(Tuple2<Long, Long> value) throws Exception {
        return value.toString();
      }
      
    });

    sink = FileSink
      .forRowFormat(
        new Path("/tmp/flink_complex_ml_benchmark_results"),
        new SimpleStringEncoder<String>("UTF-8"))
      .build();
  }

  public void run() throws Exception{
    throw new Exception("this is an abstract function, should not call this function in Pipeline base class!");
  }

  public void setModelHandler(String modelName, String handlerClass) {
    for (var node : graph.nodes()) {
      if (node.getType().equals("model") && node.getName().equals(modelName)) {
        ((ModelNode)node).setHandler(Util.experimentsHandler(id, handlerClass));
      }
    }
  }

  public void setModelHandler(ModelNode node) {
    node.setHandler(Util.experimentsHandler(id, node.getHandlerClass()));
  }

  public void init(Pipeline.Type pipelineType) throws UnknownPipelineType {
    this.type = pipelineType;
    updateNodesByType();
    setConfigPath(configPath);
    getNodes().sort(null);
    initializeGraph();
    initializeLayeredNodes();
    initializeHandlers();

    initialized = true;
  }

  private void updateNodesByType() throws UnknownPipelineType {
    List<PipelineNode> newNodes = new ArrayList<PipelineNode>();
    for (PipelineNode node : nodes) {
      switch (node.getType()) {
        case "preprocessor":
          newNodes.add(new PreprocessorNode(node));
          break;

        // default node type will be "model"
        case "model":
        default: {
          switch (type) {
            case TORCH_SERVE:
              newNodes.add(new TorchServeModelNode(node));
              break;
            default:
              throw new UnknownPipelineType("Unknown pipeline type!");
          }
        }
          break;
      }
    }

    this.nodes = newNodes;
  }

  private void initializeGraph() {
    if (nodes == null) {
      logger.error("failed to initialize graph in the pipeline, nodes is null!");
      return;
    }

    graph = GraphBuilder.directed()
                        .nodeOrder(ElementOrder.sorted(new PipelineNode.PipelineNodeComparator()))
                        .build();

    // convert nodes list to hashmap, speed up neighbor lookup
    Map<Integer, PipelineNode> mapNodes = new HashMap<>();
    for (PipelineNode node : nodes) {
      mapNodes.put(node.getId(), node);
    }

    for (PipelineNode node : nodes) {
      graph.addNode(node);
      for (Integer i : node.getNextNodes()) {
        graph.putEdge(node, mapNodes.get(i));
      }
    }

    if (!validateGraph()) {
      graph = null;
      logger.error("failed to initialize graph in the pipeline, invalid graph! (probably exist cycles?)");
      return;
    }
  }

  /**
   * BFS traverse the graph, store a layered view of nodes.
   */
  private void initializeLayeredNodes() {
    if (graph == null || !validateGraph()) {
      return;
    }

    layeredNodes = new Vector<Set<PipelineNode>>();
    layeredNodes.add(new HashSet<PipelineNode>());
    int layerIdx = 0;

    layeredNodes.get(layerIdx).add(graph.nodes().iterator().next());

    while(layerIdx < layeredNodes.size()) {
      for (var node : layeredNodes.get(layerIdx)) {
        var nextLayer = graph.successors(node);

        if (!nextLayer.isEmpty()) {
          /* Create a new layer if not exists */
          if (layerIdx+1 >= layeredNodes.size()) {
            layeredNodes.add(new HashSet<PipelineNode>());
          }

          layeredNodes.get(layerIdx+1).addAll(nextLayer);
        }
      }

      layerIdx++;
    }

    return;
  }

  private void initializeHandlers() {
    for (var node : nodes) {
      if (!node.getType().equals("model"))
        continue;
      
      setModelHandler((ModelNode)node);
    }
  }

  private static Constructor buildPipelineYamlConstructor() {
    Constructor constructor = new Constructor(Pipeline.class);
    TypeDescription pipelineDesc = new TypeDescription(Pipeline.class);
    TypeDescription nodeDesc = new TypeDescription(PipelineNode.class);
    pipelineDesc.substituteProperty("nodes", List.class, "getNodes", "setNodes", PipelineNode.class);
    nodeDesc.substituteProperty("next", List.class, "getNextNodes", "setNextNodes");
    nodeDesc.substituteProperty("model_path", String.class, "getModelPath", "setModelPath");
    nodeDesc.substituteProperty("handler_class", String.class, "getHandlerClass", "setHandlerClass");
    constructor.addTypeDescription(pipelineDesc);
    constructor.addTypeDescription(nodeDesc);

    return constructor;
  }

  /**
   * Validate a graph, check if this graph has cycles.
   * As a DAG, it should not contain any cycles.
   * @return the validation result
   */
  private boolean validateGraph() {
    return !Graphs.hasCycle(graph);
  }

  @Override
  public String toString() {
    return String.format("[Pipeline] Name: %s, Graph: %s\n", this.name, graph == null ? "null" : graph.toString());
  }

  protected DataStream<MLEventOut> modelStreamOp(ModelNode node, Set<DataStream<MLEventIn>> attachedInputStreams) {
    return null;
  }

  protected Set<DataStream<MLEventOut>> buildDAG(DataStream<MLEventIn> sourceInputStream) {
    Map<PipelineNode, Set<DataStream<MLEventIn>>> nodesAttachedInputStreams = new HashMap<>();
    
    // source stream to the starter nodes
    for (var node : layeredNodes.get(0)) {
      if (!node.getType().equals("model")) 
        continue;
      var newSrc = sourceInputStream.map(new MapFunction<MLEventIn, MLEventIn> () {
        private static final long serialVersionUID = -4772987795153271331L;
        @Override
        public MLEventIn map(MLEventIn e) throws Exception {
          var newEvent = e.dup();
          return newEvent;
        }
      });

      nodesAttachedInputStreams.put(node, new HashSet<>(Arrays.asList(newSrc)));
    }

    Set<DataStream<MLEventOut>> lastOutputStreams = new HashSet<DataStream<MLEventOut>>();
    for (var nodes : layeredNodes) {
      for (var node : nodes) {
        var attachedInputStreams = nodesAttachedInputStreams.get(node);
        
        var outputStream = modelStreamOp((ModelNode)node, attachedInputStreams);

        // if node has no successors, the output stream is the final stream
        // we need to record its finish timestamp.
        if (graph.successors(node).isEmpty()) {
          outputStream = outputStream.map(new MapFunction<MLEventOut, MLEventOut>() {
            private static final long serialVersionUID = -4498921852176831334L;
            @Override
            public MLEventOut map(MLEventOut value) throws Exception {
              value.setFinishTimestamp(System.nanoTime());
              return value;
            }
          });

          lastOutputStreams.add(outputStream);
        }

        // attach the output stream as a new input stream to the next nodes
        for (var nextNode : graph.successors(node)) {
          if (!nodesAttachedInputStreams.containsKey(nextNode)) {
            nodesAttachedInputStreams.put(nextNode, new HashSet<DataStream<MLEventIn>>());
          }
          nodesAttachedInputStreams.get(nextNode).add(mapOutStreamToInStream(outputStream, (ModelNode)node));
        }
      }
    }

    return lastOutputStreams;
  }

  protected DataStream<MLEventIn> mapOutStreamToInStream(DataStream<MLEventOut> outStream, ModelNode node) {
    return outStream.map(new MapFunction<MLEventOut, MLEventIn>() {
      private static final long serialVersionUID = 8089056025441385575L;

      @Override
      public MLEventIn map(MLEventOut e) throws Exception {
        var eventIn = e.toMLEventIn();
        eventIn.putResult(node.getName(), e.getResult());
        return eventIn;
      }
      
    });
  }

  protected DataStream<MLEventIn> connectStreams(Set<DataStream<MLEventIn>> streams) {
    if (streams.size() == 0) return null;

    var iter = streams.iterator();
    var currSt = iter.next();
    while (iter.hasNext()) {
      var nextSt = iter.next();
      currSt = currSt.connect(nextSt)
                     .flatMap(new MergeTwoStreamsFunction());
    }

    return currSt;
  }

  protected class MergeTwoStreamsFunction implements CoFlatMapFunction<MLEventIn, MLEventIn, MLEventIn> {

    private static final long serialVersionUID = 5045137104286318493L;

    private Map<Long, MLEventIn> seenEvents = new HashMap<>();

    @Override
    public void flatMap1(MLEventIn e, Collector<MLEventIn> collector) throws Exception {
      checkCollectOutput(e, collector);
    }

    @Override
    public void flatMap2(MLEventIn e, Collector<MLEventIn> collector) throws Exception {
      checkCollectOutput(e, collector);
    }

    private void checkCollectOutput(MLEventIn e, Collector<MLEventIn> collector) {
      if (seenEvents.containsKey(e.getId())) {
        // merge results from two models, key is the model name
        e.getResults().putAll(seenEvents.get(e.getId()).getResults());
        collector.collect(e);
        seenEvents.remove(e.getId());
      } else {
        seenEvents.put(e.getId(), e);
      }
    }
    
  }

  DataStream<MLEventOut> mergeResults(Set<DataStream<MLEventOut>> results) {
    var iter = results.iterator();
    var currSt = iter.next();
    while (iter.hasNext()) {
      var nextSt = iter.next();
      currSt = currSt.connect(nextSt)
                     .map(new MergeResultsFunction());
    }

    return currSt;
  }

  private class MergeResultsFunction implements CoMapFunction<MLEventOut, MLEventOut, MLEventOut> {
    private static final long serialVersionUID = 8237181024487409968L;

    @Override
    public MLEventOut map1(MLEventOut e) throws Exception {
      return e;
    }

    @Override
    public MLEventOut map2(MLEventOut e) throws Exception {
      return e;
    }

   }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getConfigPath() {
    return configPath;
  }

  public void setConfigPath(String configPath) {
    this.configPath = configPath;
  }

  public List<PipelineNode> getNodes() {
    return nodes;
  }

  public void setNodes(List<PipelineNode> nodes) {
    this.nodes = nodes;
  }

  public MutableGraph<PipelineNode> getGraph() {
    return graph;
  }

  public void setGraph(MutableGraph<PipelineNode> graph) {
    this.graph = graph;
  }

  public Pipeline.Type getType() {
    return type;
  }

  public void setType(Pipeline.Type type) {
    this.type = type;
  }

  public ComplexMLBenchmark.Type getBenchmarkType() {
    return benchmarkType;
  }

  public void setBenchmarkType(ComplexMLBenchmark.Type benchmarkType) {
    this.benchmarkType = benchmarkType;
  }

  public List<Set<PipelineNode>> getLayeredNodes() {
    return layeredNodes;
  }

  public void setLayeredNodes(List<Set<PipelineNode>> layeredNodes) {
    this.layeredNodes = layeredNodes;
  }

  public Boolean getInitialized() {
    return initialized;
  }

  public void setInitialized(Boolean initialized) {
    this.initialized = initialized;
  }

  public StreamExecutionEnvironment getEnv() {
    return env;
  }

  public void setEnv(StreamExecutionEnvironment env) {
    this.env = env;
  }

  public DataStream<String> getResultStream() {
    return resultStream;
  }

  public void setResultStream(DataStream<String> resultStream) {
    this.resultStream = resultStream;
  }

  public FileSink<String> getSink() {
    return sink;
  }

  public void setSink(FileSink<String> sink) {
    this.sink = sink;
  }

  public Generator getGenerator() {
    return generator;
  }

  public void setGenerator(Generator generator) {
    this.generator = generator;
  }

}
