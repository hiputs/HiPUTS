package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository;

import java.util.Map;

public interface SpeedLimitsRepository {

  Map<String, SpeedLimitRecord> getMapOfCountriesWithSpeedLimits();

}

