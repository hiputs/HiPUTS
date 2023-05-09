package pl.edu.agh.hiputs.visualization.web;

public class Node {
    private Coordinates coordinates;
    private String nodeId;

    public Node(Node node) {
        this.coordinates = new Coordinates(node.getCoordinates().getCoordinates().get(0),node.getCoordinates().getCoordinates().get(1));
        this.nodeId = node.getNodeId();
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
