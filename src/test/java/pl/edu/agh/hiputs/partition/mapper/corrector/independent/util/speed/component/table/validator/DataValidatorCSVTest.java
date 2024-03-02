package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.validator;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

@ExtendWith(MockitoExtension.class)
public class DataValidatorCSVTest {

  @InjectMocks
  private DataValidatorCSV dataValidatorCSV;

  private SpeedLimitRecord record1;
  private List<SpeedLimitRecord> records;

  @BeforeEach
  public void init() {
    records = new ArrayList<>();
    record1 = new SpeedLimitRecord();
    records.add(record1);
  }

  @Test
  public void checkCountryIdEmptyRemoval() {
    // given

    // when
    record1.setHighway("30");
    record1.setUrban("20");
    record1.setRural("10");
    record1.setCountryId("");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(0, records.size());
  }

  @Test
  public void checkNARemoval() {
    // given

    // when
    record1.setHighway("NA");
    record1.setUrban("n/a");
    record1.setRural("");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(0, records.size());
  }

  @Test
  public void determineWhenHighwayNAOthersAreSet() {
    // given
    String higherLimit = "50";
    String lowerLimit = "30";

    // when
    record1.setHighway("NA");
    record1.setRural(higherLimit);
    record1.setUrban(lowerLimit);
    record1.setCountryId("PL");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(higherLimit, records.get(0).getHighway());
  }

  @Test
  public void determineWhenRuralNAOthersAreSet() {
    // given
    String higherLimit = "50";
    String lowerLimit = "30";

    // when
    record1.setHighway(higherLimit);
    record1.setRural("NA");
    record1.setUrban(lowerLimit);
    record1.setCountryId("PL");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(lowerLimit, records.get(0).getRural());
  }

  @Test
  public void determineWhenUrbanNAOthersAreSet() {
    // given
    String higherLimit = "50";
    String lowerLimit = "30";

    // when
    record1.setHighway(higherLimit);
    record1.setRural(lowerLimit);
    record1.setUrban("NA");
    record1.setCountryId("PL");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(lowerLimit, records.get(0).getUrban());
  }

  @Test
  public void determineWhenHighwayRuralNAUrbanSet() {
    // given
    String lowerLimit = "30";

    // when
    record1.setHighway("NA");
    record1.setRural("n/a");
    record1.setUrban(lowerLimit);
    record1.setCountryId("PL");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(lowerLimit, records.get(0).getHighway());
    Assertions.assertEquals(lowerLimit, records.get(0).getRural());
  }

  @Test
  public void determineWhenHighwayUrbanNAUrbanSet() {
    // given
    String lowerLimit = "30";

    // when
    record1.setHighway("NA");
    record1.setRural(lowerLimit);
    record1.setUrban("n/a");
    record1.setCountryId("PL");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(lowerLimit, records.get(0).getHighway());
    Assertions.assertEquals(lowerLimit, records.get(0).getUrban());
  }

  @Test
  public void determineWhenRuralUrbanNAUrbanSet() {
    // given
    String higherLimit = "50";

    // when
    record1.setHighway(higherLimit);
    record1.setRural("NA");
    record1.setUrban("n/a");
    record1.setCountryId("PL");
    dataValidatorCSV.checkAndCorrect(records);

    // then
    Assertions.assertEquals(higherLimit, records.get(0).getRural());
    Assertions.assertEquals(higherLimit, records.get(0).getUrban());
  }

}
