package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;

public interface Decider {

  void decideAboutValue(SpeedResultHandler speedDataHandler);

}
