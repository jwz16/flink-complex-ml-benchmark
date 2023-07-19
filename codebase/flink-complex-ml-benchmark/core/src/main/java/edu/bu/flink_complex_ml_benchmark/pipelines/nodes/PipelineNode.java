package edu.bu.flink_complex_ml_benchmark.pipelines.nodes;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class PipelineNode implements Comparable<PipelineNode>, Serializable{

  private static final long serialVersionUID = -6457664514336631192L;

  protected Integer id;
  protected String type;
  protected String name;

  protected List<Integer> next;
  protected String model_path;
  protected String handler_class;

  public PipelineNode() {
    // DO NOT REMOVE, NEEDED BY SNAKE YAML
  }

  public PipelineNode(PipelineNode node) {
    this.id = node.getId();
    this.type = node.getType();
    this.name = node.getName();
    this.next = node.getNextNodes();
    this.model_path = node.getModelPath();
    this.handler_class = node.getHandlerClass();
  }

  public PipelineNode(Integer id, String name, String type) {
    this.id = id;
    this.type = type;
  }

  @Override
  public String toString() {
    return String.format("[Node] id: %d, type: %s", id, type);
  }

  public Integer getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Integer> getNextNodes() {
    return next;
  }

  public void setNextNodes(List<Integer> nextNodes) {
    this.next = nextNodes;
  }

  public String getModelPath() {
    return model_path;
  }

  public void setModelPath(String model_path) {
    this.model_path = model_path;
  }

  public String getHandlerClass() {
    return handler_class;
  }

  public void setHandlerClass(String handler) {
    this.handler_class = handler;
  }

  @Override
  public int compareTo(PipelineNode p) {
    return this.id - p.getId();
  }

  public static class PipelineNodeComparator implements Comparator<PipelineNode> {
    public int compare(PipelineNode a, PipelineNode b) {
      return a.compareTo(b);
    }
  }

}
