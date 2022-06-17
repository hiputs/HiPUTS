package pl.edu.agh.hiputs.utils;

public class CoordinatesUtil {

  private static final Double EARTH_RADIUS = 6371.0;
  public static Double latitude2plain(Double lat) {
      return EARTH_RADIUS * Math.toRadians(lat);
    }

  public static Double longitude2plain(Double lon, Double lat) {
    return EARTH_RADIUS * Math.toRadians(lon) * Math.cos(Math.toRadians(lat));
  }

  /**
   * Calculate distance between two points in latitude and longitude taking
   * into account height difference. If you are not interested in height
   * difference pass 0.0. Uses Haversine method as its base.
   * <p>
   * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
   * el2 End altitude in meters
   *
   * @returns Distance in Meters
   */
  public static double plainDistanceInMeters(double lat1, double lat2, double lon1, double lon2) {
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(
        Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS * c * 1000; // convert to meters
  }

}
