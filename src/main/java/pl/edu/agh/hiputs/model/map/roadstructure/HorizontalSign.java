package pl.edu.agh.hiputs.model.map.roadstructure;

/**
 * Horizontal signs separating lanes
 */
public enum HorizontalSign {

  /**
   * Horizontal solid line indicating separation from lane in opposite direction
   * it does not allow to cross it
   */
  OPPOSITE_DIRECTION_SOLID_LINE,

  /**
   * Horizontal dotted line indicating separation from lane in opposite direction
   * it can be crossed
   */
  OPPOSITE_DIRECTION_DOTTED_LINE
}
