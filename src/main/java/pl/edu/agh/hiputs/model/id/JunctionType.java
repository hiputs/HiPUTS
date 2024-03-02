package pl.edu.agh.hiputs.model.id;

/**
 * Junction type that can be distinguished based on Junction incoming and outgoing roads number
 */
public enum JunctionType {

  /**
   * Junction that have only one incoming and only one outgoing road
   */
  BEND,

  /**
   * Junction that have more than one incoming or/and more than one outgoing road
   */
  CROSSROAD
}
