package pl.edu.agh.hiputs.communication.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Configuration {
    private final String serverAddress;

}
