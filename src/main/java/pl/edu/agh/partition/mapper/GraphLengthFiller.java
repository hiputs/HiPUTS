package pl.edu.agh.partition.mapper;

import pl.edu.agh.partition.model.Edge;
import pl.edu.agh.partition.model.Graph;

class GraphLengthFiller implements GraphTransformer {

    @Override
    public Graph transform(Graph graph) {
        for (Edge edge : graph.getEdges().values()) {
            double length = calculateLength(edge);
            edge.setLength(length);
        }
        return graph;
    }

    private double calculateLength(Edge edge) {
        return distance(
                edge.getSource().getLat(),
                edge.getTarget().getLat(),
                edge.getSource().getLon(),
                edge.getTarget().getLon());
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    public double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return Math.sqrt(distance);
    }


}
