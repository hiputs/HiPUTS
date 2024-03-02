package pl.edu.agh.hiputs.communication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeighbourConnectionDto {
  private String id;
  private String address;
  private int port;
}
