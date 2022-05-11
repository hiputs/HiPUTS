package pl.edu.agh.utils;

public class CoordinatesUtil {

  private static final Double EARTH_RADIUS = 6371.0;
  public static Double latitude2plain(Double lat) {
      return EARTH_RADIUS * Math.toRadians(lat);
    }

  public static Double longitude2plain(Double lon, Double lat) {
    return EARTH_RADIUS * Math.toRadians(lon) * Math.cos(Math.toRadians(lat));
  }
    
}
