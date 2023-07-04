package pl.edu.agh.hiputs.partition.osm;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * This class is responsible for reading road map from osm map, so include filtering nodes that are not included in any raod
 */
@Service
public class OsmGraphReaderImpl implements OsmGraphReader{

    public OsmGraph loadOsmData(InputStream osmFileInputStream) {
        List<OsmNode> nodes = new LinkedList<>();
        List<OsmWay> ways = new LinkedList<>();
        List<OsmRelation> relations = new LinkedList<>();

        OsmIterator iterator = new OsmXmlIterator(osmFileInputStream, false);

        for (EntityContainer container : iterator) {
            if (container.getType() == EntityType.Node) {
                OsmNode node = (OsmNode) container.getEntity();
                nodes.add(node);
            } else if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();
                for(int i=0; i<way.getNumberOfTags(); i++){
                    if (way.getTag(i).getKey().equals("highway") && !way.getTag(i).getValue().equals("footway")) {
                        ways.add(way);
                    }
                }
            } else if (container.getType() == EntityType.Relation) {
                OsmRelation relation = (OsmRelation) container.getEntity();
                for (int i = 0; i < relation.getNumberOfTags(); i++) {
                    if (relation.getTag(i).getKey().equals("type") && relation.getTag(i).getValue().equals("restriction")) {
                        relations.add(relation);
                    }
                }
            }
        }

        Set<Long> nodesIdsOnWays = ways.stream()
                .flatMap(this::getAllNodesIdsOnWay)
                .collect(Collectors.toSet());

        nodes = nodes.stream()
                .filter(osmNode -> nodesIdsOnWays.contains(osmNode.getId()))
                .collect(Collectors.toList());

        return new OsmGraph(nodes, ways, relations);
    }

    private Stream<Long> getAllNodesIdsOnWay(OsmWay way) {
        List<Long> nodeIds = new LinkedList<>();
        for(int i=0; i<way.getNumberOfNodes();i++) {
            nodeIds.add(way.getNodeId(i));
        }
        return nodeIds.stream();
    }

}
