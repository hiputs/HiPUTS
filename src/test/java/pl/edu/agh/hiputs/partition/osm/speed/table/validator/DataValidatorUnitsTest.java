package pl.edu.agh.hiputs.partition.osm.speed.table.validator;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.speed.table.repository.SpeedLimitRecord;

@ExtendWith(MockitoExtension.class)
public class DataValidatorUnitsTest {
  @InjectMocks
  private DataValidatorUnits dataValidatorUnits;

  private List<SpeedLimitRecord> records;
  private SpeedLimitRecord record1;

  @BeforeEach
  public void init() {
    records = new ArrayList<>();
    record1 = new SpeedLimitRecord();
    record1.setHighway("1000");
    record1.setRural("100");
    record1.setUrban("10");
    records.add(record1);
  }

  @Test
  public void omitKphCorrection() {
    // given
    record1.setSpeedUnit("kph");

    // when
    dataValidatorUnits.checkAndCorrect(records);

    // then
    Assertions.assertEquals("1000", records.get(0).getHighway());
    Assertions.assertEquals("100", records.get(0).getRural());
    Assertions.assertEquals("10", records.get(0).getUrban());
  }

  @Test
  public void changeFromMphToKph() {
    // given
    record1.setSpeedUnit("mph");

    // when
    dataValidatorUnits.checkAndCorrect(records);

    // then
    Assertions.assertEquals("1609", records.get(0).getHighway());
    Assertions.assertEquals("160", records.get(0).getRural());
    Assertions.assertEquals("16", records.get(0).getUrban());
  }
}

