package pl.edu.agh.hiputs.partition.osm;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
public class OsmGraph {

    private List<OsmNode> nodes;
    private List<OsmWay> ways;

}
