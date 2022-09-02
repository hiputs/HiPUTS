package pl.edu.agh.hiputs.partition.osm.speed.rule.resolver;

import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.countryKeyInTags;

import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultCountryResolverTest {
  @InjectMocks
  private DefaultCountryResolver defaultCountryResolver;

  private List<OsmNode> nodes;
  private OsmNode node1, node2, node3;

  @BeforeEach
  public void init() {
    nodes = new ArrayList<>();
  }

  @Test
  public void findValueInNodesWithoutRequiredCountryTag() {
    // given
    node1 = new Node(1L, 1.0, 1.0, List.of(new Tag("k1", "v1")), Mockito.mock(OsmMetadata.class));
    nodes.add(node1);

    // when
    defaultCountryResolver.findValue(nodes);

    // then
    Assertions.assertEquals("DEFAULT", defaultCountryResolver.getValue());
  }

  @Test
  public void findValueInNodesPartiallyWithoutRequiredCountryTag() {
    // given
    node1 = new Node(1L, 1.0, 1.0, List.of(new Tag("k1", "v1")), Mockito.mock(OsmMetadata.class));
    node2 = new Node(2L, 2.0, 2.0, List.of(new Tag("k2", "v2"), new Tag(countryKeyInTags, "PL")),
        Mockito.mock(OsmMetadata.class));
    node3 = new Node(3L, 3.0, 3.0, List.of(new Tag("k3", "v3")), Mockito.mock(OsmMetadata.class));
    nodes.add(node1);
    nodes.add(node2);
    nodes.add(node3);

    // when
    defaultCountryResolver.findValue(nodes);

    // then
    Assertions.assertEquals("PL", defaultCountryResolver.getValue());
  }

  @Test
  public void findValueInNodesWithRequiredCountryTag() {
    // given
    node1 = new Node(1L, 1.0, 1.0, List.of(new Tag("k1", "v1"), new Tag(countryKeyInTags, "UK")),
        Mockito.mock(OsmMetadata.class));
    node2 = new Node(2L, 2.0, 2.0, List.of(new Tag("k2", "v2"), new Tag(countryKeyInTags, "PL")),
        Mockito.mock(OsmMetadata.class));
    node3 = new Node(3L, 3.0, 3.0, List.of(new Tag("k3", "v3"), new Tag(countryKeyInTags, "PL")),
        Mockito.mock(OsmMetadata.class));
    nodes.add(node1);
    nodes.add(node2);
    nodes.add(node3);

    // when
    defaultCountryResolver.findValue(nodes);

    // then
    Assertions.assertEquals("PL", defaultCountryResolver.getValue());
  }
}
