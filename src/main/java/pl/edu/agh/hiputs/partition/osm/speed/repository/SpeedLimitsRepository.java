package pl.edu.agh.hiputs.partition.osm.speed.repository;

import java.util.Map;

public interface SpeedLimitsRepository {

  Map<String, SpeedLimitRecord> getMapOfCountriesWithSpeedLimits();

}
