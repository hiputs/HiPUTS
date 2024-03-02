package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.pairing;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class DefaultPairingIncomingWithOutgoingsTest {
  private final DefaultPairingIncomingWithOutgoings allocator = new DefaultPairingIncomingWithOutgoings();

  @Test
  public void pairWhenNoIncomingLanes() {
    // given
    WayData incoming = WayData.builder().build();
    LaneData outgoingLane1 = LaneData.builder().build();
    LaneData outgoingLane2 = LaneData.builder().build();
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().lanes(List.of(outgoingLane1)).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().lanes(List.of(outgoingLane2)).build());

    // when
    allocator.pair(incoming, List.of(edge1, edge2));

    // then
    Assertions.assertEquals(0, incoming.getLanes().stream()
        .mapToLong(laneData -> laneData.getAvailableSuccessors().size())
        .sum());
  }

  @Test
  public void pairWhenNoOutgoingLanes() {
    // given
    LaneData incomingLane1 = LaneData.builder().build();
    LaneData incomingLane2 = LaneData.builder().build();
    WayData incoming = WayData.builder().lanes(List.of(incomingLane1, incomingLane2)).build();
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());

    // when
    allocator.pair(incoming, List.of(edge1, edge2));

    // then
    Assertions.assertEquals(0, incoming.getLanes().stream()
        .mapToLong(laneData -> laneData.getAvailableSuccessors().size())
        .sum());
  }

  @Test
  public void pairMoreOutgoingEdgesThanIncoming() {
    // given
    LaneData incomingLane1 = LaneData.builder().build();
    LaneData incomingLane2 = LaneData.builder().build();
    LaneData outgoingLane1 = LaneData.builder().build();
    LaneData outgoingLane2 = LaneData.builder().build();
    WayData incoming = WayData.builder().lanes(List.of(incomingLane1, incomingLane2)).build();
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().lanes(List.of(outgoingLane1)).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().lanes(List.of(outgoingLane2)).build());

    // when
    allocator.pair(incoming, List.of(edge1, edge2));

    // then
    Assertions.assertEquals(outgoingLane1, incomingLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outgoingLane2, incomingLane2.getAvailableSuccessors().get(0));
  }

  @Test
  public void pairMoreOutgoingLanesThanIncoming() {
    // given
    LaneData incomingLane1 = LaneData.builder().build();
    LaneData incomingLane2 = LaneData.builder().build();
    LaneData outgoingLane1 = LaneData.builder().build();
    LaneData outgoingLane2 = LaneData.builder().build();
    LaneData outgoingLane3 = LaneData.builder().build();
    WayData incoming = WayData.builder().lanes(List.of(incomingLane1, incomingLane2)).build();
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .lanes(List.of(outgoingLane1, outgoingLane2, outgoingLane3)).build());

    // when
    allocator.pair(incoming, List.of(edge));

    // then
    Assertions.assertEquals(outgoingLane1, incomingLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outgoingLane2, incomingLane1.getAvailableSuccessors().get(1));
    Assertions.assertEquals(outgoingLane3, incomingLane2.getAvailableSuccessors().get(0));
  }

  @Test
  public void pairMoreIncomingLanesThanOutgoing() {
    // given
    LaneData incomingLane1 = LaneData.builder().build();
    LaneData incomingLane2 = LaneData.builder().build();
    LaneData incomingLane3 = LaneData.builder().build();
    LaneData outgoingLane1 = LaneData.builder().build();
    LaneData outgoingLane2 = LaneData.builder().build();
    WayData incoming = WayData.builder().lanes(List.of(incomingLane1, incomingLane2, incomingLane3)).build();
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .lanes(List.of(outgoingLane1, outgoingLane2)).build());

    // when
    allocator.pair(incoming, List.of(edge));

    // then
    Assertions.assertEquals(outgoingLane1, incomingLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outgoingLane1, incomingLane2.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outgoingLane2, incomingLane3.getAvailableSuccessors().get(0));
  }
}
