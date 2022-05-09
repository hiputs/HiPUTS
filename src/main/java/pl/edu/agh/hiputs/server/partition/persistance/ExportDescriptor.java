package pl.edu.agh.hiputs.server.partition.persistance;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportDescriptor {

  @Builder.Default
  private String nodesFileName = "nodes.csv";

  @Builder.Default
  private String edgesFileName = "edges.csv";

  @Builder.Default
  private String patchesFileName = "patches.csv";

  static final String NODES_HEADER = "id,longitude,latitude,patch_id,tags";
  static final String EDGES_HEADER = "source,target,length,max_speed,patch_id,tags";
  static final String PATCHES_HEADER = "id,neighbouring_patches_ids";

}
