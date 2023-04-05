package pl.edu.agh.hiputs.partition.persistance;

import lombok.Builder;

@Builder
class ExportDescriptor {

  private static final String nodesFileName = "nodes.csv";

  private static final String edgesFileName = "edges.csv";

  private static final String lanesFileName = "lanes.csv";

  private static final String patchesFileName = "patches.csv";

  private String exportDirAbsolutePath;

  public String getNodesFilePath() {
    return exportDirAbsolutePath + "/" + nodesFileName;
  }

  public String getEdgesFilePath() {
    return exportDirAbsolutePath + "/" + edgesFileName;
  }

  public String getLanesFilePath() {
    return exportDirAbsolutePath + "/" + lanesFileName;
  }

  public String getPatchesFilePath() {
    return exportDirAbsolutePath + "/" + patchesFileName;
  }
}
