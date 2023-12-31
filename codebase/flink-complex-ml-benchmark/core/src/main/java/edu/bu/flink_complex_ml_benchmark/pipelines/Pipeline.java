package edu.bu.flink_complex_ml_benchmark.pipelines;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
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

import edu.bu.flink_complex_ml_benchmark.ComplexMLBenchmark;
import edu.bu.flink_complex_ml_benchmark.Config;
import edu.bu.flink_complex_ml_benchmark.Util;
import edu.bu.flink_complex_ml_benchmark.connectors.Generator;
import edu.bu.flink_complex_ml_benchmark.connectors.MLSyntheticImageBatchesSource;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEvent;
import edu.bu.flink_complex_ml_benchmark.connectors.events.MLEventSchema;
import edu.bu.flink_complex_ml_benchmark.exceptions.UnknownPipelineType;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ModelNode;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.ONNXModelNode;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.PipelineNode;
import edu.bu.flink_complex_ml_benchmark.pipelines.nodes.TorchServeModelNode;

public class Pipeline {
  protected static Logger logger = LoggerFactory.getLogger(Pipeline.class);

  public enum Type {
    TORCH_SERVE, TF_SERVE, ONNX, ND4J, TF_SAVED_MODEL
  }

  protected StreamExecutionEnvironment env;
  protected DataStream<String> resultStream;
  // protected FileSink<String> sink;
  protected KafkaSink<String> sink;
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

  /**
   * Build a new pipeline object based on pipeline definition file and pipeline type
   * @param configPath Pipeline Yaml definition file path
   * @param pipelineType
   * @return Return a new pipeline object if success, otherwise return null
   */
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
    // Set up the streaming execution environment
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    Util.registerProtobufType(env);

    // DataStream<MLEvent> inputStream = inputStreamFromLocalDataSource(env);
    DataStream<MLEvent> inputStream = inputStreamFromKafkaDataSource(env);
    
    // build DAG datastream flow
    var allResults = buildDAG(inputStream);

    // Benchmarking - record timestamp when the scoring is done
    var results = mergeStreams(allResults);

    DataStream<Tuple2<Long, Long>> records = results.map(new MapFunction<MLEvent, Tuple2<Long, Long>>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Tuple2<Long, Long> map(MLEvent e) throws Exception {
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

    // sink = FileSink
    //   .forRowFormat(
    //     new Path("/tmp/flink_complex_ml_benchmark_results"),
    //     new SimpleStringEncoder<String>("UTF-8"))
    //   .build();

    sink = KafkaSink.<String>builder()
        .setBootstrapServers("localhost:9094")
        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
            .setTopic("complex-ml-output")
            .setValueSerializationSchema(new SimpleStringSchema())
            .build()
        )
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
        // default node type will be "model"
        case "model":
        default: {
          switch (type) {
            case TORCH_SERVE:
              newNodes.add(new TorchServeModelNode(node));
              break;
            case ONNX:
              newNodes.add(new ONNXModelNode(node));
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

  protected DataStream<MLEvent> modelStreamOp(ModelNode node, Set<DataStream<MLEvent>> attachedInputStreams) {
    return null;
  }

  protected Set<DataStream<MLEvent>> buildDAG(DataStream<MLEvent> sourceInputStream) {
    Map<PipelineNode, Set<DataStream<MLEvent>>> nodesAttachedInputStreams = new HashMap<>();
    
    // source stream to the starter nodes
    for (var node : layeredNodes.get(0)) {
      if (!node.getType().equals("model")) 
        continue;
      var newSrc = sourceInputStream.map(new MapFunction<MLEvent, MLEvent> () {
        private static final long serialVersionUID = -4772987795153271331L;
        @Override
        public MLEvent map(MLEvent e) throws Exception {
          var newEvent = e.dup();
          return newEvent;
        }
      });

      nodesAttachedInputStreams.put(node, new HashSet<>(Arrays.asList(newSrc)));
    }

    Set<DataStream<MLEvent>> lastOutputStreams = new HashSet<DataStream<MLEvent>>();
    for (var nodes : layeredNodes) {
      for (var node : nodes) {
        var attachedInputStreams = nodesAttachedInputStreams.get(node);
        
        ((ModelNode)node).setFramework(type);
        var outputStream = modelStreamOp((ModelNode)node, attachedInputStreams);

        // if node has no successors, the output stream is the final stream
        // we need to record its finish timestamp.
        if (graph.successors(node).isEmpty()) {
          outputStream = outputStream.map(new MapFunction<MLEvent, MLEvent>() {
            private static final long serialVersionUID = -4498921852176831334L;
            @Override
            public MLEvent map(MLEvent value) throws Exception {
              value.setFinishTimestamp(System.nanoTime());
              return value;
            }
          });

          lastOutputStreams.add(outputStream);
        }

        // attach the output stream as a new input stream to the next nodes
        for (var nextNode : graph.successors(node)) {
          if (!nodesAttachedInputStreams.containsKey(nextNode)) {
            nodesAttachedInputStreams.put(nextNode, new HashSet<DataStream<MLEvent>>());
          }
          nodesAttachedInputStreams.get(nextNode).add(outputStream);
        }
      }
    }

    return lastOutputStreams;
  }

  protected DataStream<MLEvent> mergeStreams(Set<DataStream<MLEvent>> streams) {
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

  protected class MergeTwoStreamsFunction implements CoFlatMapFunction<MLEvent, MLEvent, MLEvent> {

    private static final long serialVersionUID = 5045137104286318493L;

    private Map<Long, MLEvent> seenEvents = new HashMap<>();

    @Override
    public void flatMap1(MLEvent e, Collector<MLEvent> collector) throws Exception {
      checkCollectOutput(e, collector);
    }

    @Override
    public void flatMap2(MLEvent e, Collector<MLEvent> collector) throws Exception {
      checkCollectOutput(e, collector);
    }

    private void checkCollectOutput(MLEvent e, Collector<MLEvent> collector) {
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

  private DataStream<MLEvent> inputStreamFromLocalDataSource(StreamExecutionEnvironment env) {
    var config = Config.getInstance();

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

    return env.addSource(inputSrc);
  }

  /**
   * Use Kafka as data source, so Flink can work as a consumer
   * @param env 
   * @return DataStream
   */
  private DataStream<MLEvent> inputStreamFromKafkaDataSource(StreamExecutionEnvironment env) {
    KafkaSource<MLEvent> source = KafkaSource.<MLEvent>builder()
    .setBootstrapServers("localhost:9094")
    .setTopics("complex-ml-input")
    .setGroupId("complex-ml-input-consumer")
    .setStartingOffsets(OffsetsInitializer.earliest())
    .setValueOnlyDeserializer(new MLEventSchema())
    .build();

    return env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
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

  // public FileSink<String> getSink() {
  //   return sink;
  // }

  // public void setSink(FileSink<String> sink) {
  //   this.sink = sink;
  // }

  public KafkaSink<String> getSink() {
    return sink;
  }

  public void setSink(KafkaSink<String> sink) {
    this.sink = sink;
  }

  public Generator getGenerator() {
    return generator;
  }

  public void setGenerator(Generator generator) {
    this.generator = generator;
  }

}
