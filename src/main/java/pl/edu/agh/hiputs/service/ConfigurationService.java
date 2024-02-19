package pl.edu.agh.hiputs.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.configuration.Configuration;

@Service
public class ConfigurationService {

  @Getter
  private static Configuration configuration;

  @Autowired
  public void setConfiguration(Configuration configuration) {
    ConfigurationService.configuration = configuration;
  }

}
