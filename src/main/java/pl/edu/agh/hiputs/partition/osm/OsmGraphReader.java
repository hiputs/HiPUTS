package pl.edu.agh.hiputs.partition.osm;

import java.io.InputStream;

public interface OsmGraphReader {

  OsmGraph loadOsmData(InputStream osmFileInputStream);

}
