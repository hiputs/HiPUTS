package pl.edu.agh.hiputs.communication.model.serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.NeighbourConnectionDto;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDto implements CustomSerializable<NeighbourConnectionDto>{

  private String address;
  private int port;
  private String id;

  @Override
  public NeighbourConnectionDto toRealObject() {
    return NeighbourConnectionDto.builder()
        .address(address)
        .port(port)
        .id(id)
        .build();
  }
}
