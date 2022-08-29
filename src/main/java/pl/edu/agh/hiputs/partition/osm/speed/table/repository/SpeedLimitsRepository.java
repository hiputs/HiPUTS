package pl.edu.agh.hiputs.partition.osm.speed.table.repository;

import java.util.Map;

public interface SpeedLimitsRepository {

  Map<String, SpeedLimitRecord> getMapOfCountriesWithSpeedLimits();

}
