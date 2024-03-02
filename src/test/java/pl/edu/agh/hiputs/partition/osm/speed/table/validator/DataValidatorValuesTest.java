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
public class DataValidatorValuesTest {
  @InjectMocks
  private DataValidatorValues dataValidatorValues;

  private List<SpeedLimitRecord> records;
  private SpeedLimitRecord record1;

  @BeforeEach
  public void init() {
    records = new ArrayList<>();
    record1 = new SpeedLimitRecord();
    records.add(record1);
  }

  @Test
  public void correctValueWithDash() {
    // given
    record1.setHighway("120-140");
    record1.setRural("90-100");
    record1.setUrban("30-50");

    // when
    dataValidatorValues.checkAndCorrect(records);

    // then
    Assertions.assertEquals("140", records.get(0).getHighway());
    Assertions.assertEquals("100", records.get(0).getRural());
    Assertions.assertEquals("50", records.get(0).getUrban());
  }

  @Test
  public void doNotcorrectValueWithoutDash() {
    // given
    record1.setHighway("140");
    record1.setRural("90");
    record1.setUrban("50");

    // when
    dataValidatorValues.checkAndCorrect(records);

    // then
    Assertions.assertEquals("140", records.get(0).getHighway());
    Assertions.assertEquals("90", records.get(0).getRural());
    Assertions.assertEquals("50", records.get(0).getUrban());
  }
}
