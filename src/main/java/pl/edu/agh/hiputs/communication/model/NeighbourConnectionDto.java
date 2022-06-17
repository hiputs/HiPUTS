package pl.edu.agh.hiputs.communication.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NeighbourConnectionDto {
  private String id;
  private String address;
  private int port;
}
