package edu.bu.cs551.team8;

import java.io.IOException;

import org.apache.flink.api.java.utils.ParameterTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bu.cs551.team8.pipelines.Pipeline;

public class Config {
  private static Logger logger = LoggerFactory.getLogger(Config.class);
  private static Config gInstance = null;

  private final String propertiesFilePath = "/config.properties";

  private ComplexMLBenchmark.Type benchmarkType;
  private Pipeline.Type pipelineType;
  private String pipelineConfigPath;
  private int batchSize;
  private int experimentTimeInSeconds;
  private int warmupRequestsNumber;
  private int inputRate;
  private int parallelism;
  private boolean forceSyncRequest;
  private int maxInputRatePerThread;
  private String torchServeHost;
  private int imageSize;

  public static Config getInstance() {
    if (gInstance == null) {
      gInstance = new Config();
    }
    return gInstance;
  }

  public Config() {
    try {
      loadPropertiesFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void loadPropertiesFile() throws IOException {
    ParameterTool parameters = ParameterTool.fromPropertiesFile(getClass().getResourceAsStream(propertiesFilePath));

    pipelineConfigPath = parameters.get("pipelineConfigPath", "");
    // pipelineConfigPath = getClass().getClassLoader().getResource(pipelineConfigPath).getPath();

    String benchmarkTypeStr = parameters.getRequired("benchmarkType");
    String pipelineTypeStr = parameters.getRequired("pipelineType");
    parseTypes(benchmarkTypeStr, pipelineTypeStr);

    batchSize = parameters.getInt("batchSize", 1);
    experimentTimeInSeconds = parameters.getInt("experimentTimeInSeconds", 10);
    warmupRequestsNumber = parameters.getInt("warmupRequestsNumber", 50);
    inputRate = parameters.getInt("inputRate", 10);
    parallelism = parameters.getInt("parallelism", 1);
    forceSyncRequest = parameters.getBoolean("forceSyncRequest", false);
    maxInputRatePerThread = parameters.getInt("maxInputRatePerThread", 5000);
    torchServeHost = parameters.get("torchServeHost", "localhost");
    imageSize = parameters.getInt("inputSize", 224);
  }

  private void parseTypes(String benchmarkTypeStr, String pipelineTypeStr) {
    switch (benchmarkTypeStr) {
      case "embedded":
        this.benchmarkType = ComplexMLBenchmark.Type.EMBEDDED;
        break;
      case "external":
        this.benchmarkType = ComplexMLBenchmark.Type.EXTERNAL;
        break;
      default:
        logger.error("wrong benchmarkType: %s, it should be either embedded or external", benchmarkTypeStr);
        break;
    }

    switch (pipelineTypeStr) {
      case "onnx":
        this.pipelineType = Pipeline.Type.ONNX;
        break;
      case "nd4j":
        this.pipelineType = Pipeline.Type.ND4J;
        break;
      case "tf-saved":
        this.pipelineType = Pipeline.Type.TF_SAVED_MODEL;
        break;
      case "torchserve":
        this.pipelineType = Pipeline.Type.TORCH_SERVE;
        break;
      case "tfserve":
        this.pipelineType = Pipeline.Type.TF_SERVE;
        break;
      default:
        logger.error("wrong pipelineType: %s, it should be [onnx|nd4j|tf-saved|torchserve|tfserve] !", pipelineType);
        break;
    }
  }

  public ComplexMLBenchmark.Type getBenchmarkType() {
    return benchmarkType;
  }

  public void setBenchmarkType(ComplexMLBenchmark.Type benchmarkType) {
    this.benchmarkType = benchmarkType;
  }

  public Pipeline.Type getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(Pipeline.Type pipelineType) {
    this.pipelineType = pipelineType;
  }

  public String getPipelineConfigPath() {
    return pipelineConfigPath;
  }

  public void setPipelineConfigPath(String pipelineConfigPath) {
    this.pipelineConfigPath = pipelineConfigPath;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
  
  public int getExperimentTimeInSeconds() {
    return experimentTimeInSeconds;
  }

  public void setExperimentTimeInSeconds(int experimentTimeInSeconds) {
    this.experimentTimeInSeconds = experimentTimeInSeconds;
  }

  public int getWarmupRequestsNumber() {
    return warmupRequestsNumber;
  }

  public void setWarmupRequestsNumber(int warmupRequestsNumber) {
    this.warmupRequestsNumber = warmupRequestsNumber;
  }

  public int getInputRate() {
    return inputRate;
  }

  public void setInputRate(int inputRate) {
    this.inputRate = inputRate;
  }

  public int getParallelism() {
    return parallelism;
  }

  public void setParallelism(int parallelism) {
    this.parallelism = parallelism;
  }

  public boolean isForceSyncRequest() {
    return forceSyncRequest;
  }

  public void setForceSyncRequest(boolean forceSyncRequest) {
    this.forceSyncRequest = forceSyncRequest;
  }

  public int getMaxInputRatePerThread() {
    return maxInputRatePerThread;
  }

  public void setMaxInputRatePerThread(int maxInputRatePerThread) {
    this.maxInputRatePerThread = maxInputRatePerThread;
  }

  public String getTorchServeHost() {
    return torchServeHost;
  }

  public void setTorchServeHost(String torchServeHost) {
    this.torchServeHost = torchServeHost;
  }

  public int getImageSize() {
    return imageSize;
  }

  public void setImageSize(int imageSize) {
    this.imageSize = imageSize;
  }

}
