package pl.edu.agh.hiputs.partition.persistance;

import lombok.Builder;

@Builder
class ExportDescriptor {

  private static final String nodesFileName = "nodes.csv";

  private static final String edgesFileName = "edges.csv";

  private static final String patchesFileName = "patches.csv";

  private String exportDirAbsolutePath;

  public String getNodesFilePath() {
    return exportDirAbsolutePath + "/" + nodesFileName;
  }

  public String getEdgesFilePath() {
    return exportDirAbsolutePath + "/" + edgesFileName;
  }

  public String getPatchesFilePath() {
    return exportDirAbsolutePath + "/" + patchesFileName;
  }
}
