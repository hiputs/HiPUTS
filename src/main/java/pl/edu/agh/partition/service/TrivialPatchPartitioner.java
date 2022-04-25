package pl.edu.agh.partition.service;

import java.util.UUID;
import pl.edu.agh.partition.model.Graph;

public class TrivialPatchPartitioner implements PatchPartitioner {

  @Override
  public void partition(Graph graph) {
    graph.getEdges().values().forEach(e -> e.setPatchId(randomPatchId()));
  }

  private String randomPatchId() {
    return UUID.randomUUID().toString();
  }
}
