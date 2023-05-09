package pl.edu.agh.hiputs.utils;

import pl.edu.agh.hiputs.visualization.web.Coordinates;

public class CoordinatesUtil {

  private static final Double EARTH_RADIUS = 6371.0;

  public static Double latitude2plain(Double lat) {
    return EARTH_RADIUS * Math.toRadians(lat) * 1000;
  }

  public static Double plain2Latitude(Double y) {
    return Math.toDegrees(y / (1000 * EARTH_RADIUS));
  }

  public static Double longitude2plain(Double lon, Double lat) {
    return EARTH_RADIUS * Math.toRadians(lon) * Math.cos(Math.toRadians(lat)) * 1000;
  }

  public static Double plain2Longitude(Double x, Double y) {
    return Math.toDegrees(x/(1000 * EARTH_RADIUS * Math.cos(Math.toRadians(plain2Latitude(y)))));
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

  public static Coordinates getCoordinatesFromTwoPointAndDistance(Coordinates start,Coordinates end, double distanceFromStart){
    // Convert microdegrees to radians
    double alatRad=Math.toRadians(start.getCoordinates().get(0)/1000000);
    double alonRad=Math.toRadians(start.getCoordinates().get(1)/1000000);
    double blatRad=Math.toRadians(end.getCoordinates().get(0)/1000000);
    double blonRad=Math.toRadians(end.getCoordinates().get(1)/1000000);
    // Calculate distance in longitude
    double dlon=blonRad-alonRad;
    // Calculate common variables
    double alatRadSin=Math.sin(alatRad);
    double blatRadSin=Math.sin(blatRad);
    double alatRadCos=Math.cos(alatRad);
    double blatRadCos=Math.cos(blatRad);
    double dlonCos=Math.cos(dlon);
    // Find distance from A to B
    double distance=Math.acos(alatRadSin*blatRadSin +
            alatRadCos*blatRadCos *
                    dlonCos);
    // Find bearing from A to B
    double bearing=Math.atan2(
            Math.sin(dlon) * blatRadCos,
            alatRadCos*blatRadSin -
                    alatRadSin*blatRadCos*dlonCos);
    // Find new point
    double angularDistance=distance*distanceFromStart;
    double angDistSin=Math.sin(angularDistance);
    double angDistCos=Math.cos(angularDistance);
    double xlatRad = Math.asin( alatRadSin*angDistCos +
            alatRadCos*angDistSin*Math.cos(bearing) );
    double xlonRad = alonRad + Math.atan2(
            Math.sin(bearing)*angDistSin*alatRadCos,
            angDistCos-alatRadSin*Math.sin(xlatRad));
    // Convert radians to microdegrees
    double xlat=Math.toDegrees(xlatRad)*1000000;
    double xlon=Math.toDegrees(xlonRad)*1000000;
    if(xlat>90000000)xlat=90000000;
    if(xlat<-90000000)xlat=-90000000;
    while(xlon>180000000)xlon-=360000000;
    while(xlon<=-180000000)xlon+=360000000;
    return new Coordinates(xlat,xlon);
  }

}
