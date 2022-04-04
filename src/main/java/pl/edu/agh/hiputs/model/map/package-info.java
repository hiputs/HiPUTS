/**
 * Data related to map.
 * <p>Infrastructure:</p>
 * <p>1. Lane - single lane at system (if road is bidirectional, then there is lane in opposite direction </p>
 * <p>2. Junction - something that connects lanes - it can be crossroad, but not necessarily </p>
 * <p>Patch - fragment of the map that aggregates all Lanes and Junctions within this patch of map </p>
 */
package pl.edu.agh.hiputs.model.map;

