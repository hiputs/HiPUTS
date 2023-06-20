package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

@ExtendWith(MockitoExtension.class)
public class DataValidatorDefaultTest {
  @InjectMocks
  private DataValidatorDefault dataValidatorDefault;

  private SpeedLimitRecord record1;
  private List<SpeedLimitRecord> records;

  @BeforeEach
  public void init() {
    records = new ArrayList<>();
    record1 = new SpeedLimitRecord();
    records.add(record1);
  }

  @Test
  public void throwExceptionWhenCannotGetHighwayBest() {
    // given
    record1.setHighway("notCreatable1");

    // when

    // then
    Assertions.assertThrows(NoSuchElementException.class, () -> dataValidatorDefault.checkAndCorrect(records));
  }

  @Test
  public void throwExceptionWhenCannotGetRuralBest() {
    // given
    record1.setRural("notCreatable2");

    // when

    // then
    Assertions.assertThrows(NoSuchElementException.class, () -> dataValidatorDefault.checkAndCorrect(records));
  }

  @Test
  public void throwExceptionWhenCannotGetUrbanBest() {
    // given
    record1.setUrban("notCreatable3");

    // when

    // then
    Assertions.assertThrows(NoSuchElementException.class, () -> dataValidatorDefault.checkAndCorrect(records));
  }

  @Test
  public void throwExceptionWhenValidatingEmptyList() {
    // given

    // when

    // then
    Assertions.assertThrows(NoSuchElementException.class, () -> dataValidatorDefault.checkAndCorrect(
        Collections.emptyList()));
  }

  @Test
  public void validateAndAddDefaultValue() {
    // given
    SpeedLimitRecord record2 = new SpeedLimitRecord();
    SpeedLimitRecord record3 = new SpeedLimitRecord();
    records.add(record2);
    records.add(record3);
    record1.setHighway("90");
    record2.setHighway("80");
    record3.setHighway("90");
    record1.setRural("70");
    record2.setRural("70");
    record3.setRural("50");
    record1.setUrban("60");
    record2.setUrban("50");
    record3.setUrban("50");

    // when
    dataValidatorDefault.checkAndCorrect(records);

    // then
    Assertions.assertEquals(4, records.size());
    Assertions.assertEquals("90", records.get(3).getHighway());
    Assertions.assertEquals("70", records.get(3).getRural());
    Assertions.assertEquals("50", records.get(3).getUrban());
  }

}
