package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeedLimitRecord {

  @CsvBindByName(column = "CountryID")
  private String countryId;
  @CsvBindByName(column = "Country")
  private String country;
  @CsvBindByName(column = "Highway")
  private String highway;
  @CsvBindByName(column = "Rural")
  private String rural;
  @CsvBindByName(column = "Urban")
  private String urban;
  @CsvBindByName(column = "SpeedUnit")
  private String speedUnit;
}

