package pl.edu.agh.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

public class CoordinatesUtilTest {

  @Test
  public void latitudeConversions(){
    double latitude = 12.0;
    double y = CoordinatesUtil.latitude2plain(latitude);
    Assertions.assertThat(CoordinatesUtil.plain2Latitude(y)).isEqualTo(latitude);
  }

  @Test
  public void longitudeConversions(){
    double latitude = 10.0;
    double longitude = 12.0;
    double x = CoordinatesUtil.longitude2plain(longitude, latitude);
    double y = CoordinatesUtil.latitude2plain(latitude);
    Assertions.assertThat(CoordinatesUtil.plain2Longitude(x, y)).isEqualTo(longitude);
  }

}
