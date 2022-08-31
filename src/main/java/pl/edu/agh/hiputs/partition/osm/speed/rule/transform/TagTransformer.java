package pl.edu.agh.hiputs.partition.osm.speed.rule.transform;

import de.topobyte.osm4j.core.model.iface.OsmWay;

public interface TagTransformer {

  OsmWay replaceValue(OsmWay way, String value);

}
