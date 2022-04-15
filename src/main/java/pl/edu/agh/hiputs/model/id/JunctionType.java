package pl.edu.agh.hiputs.model.id;

/**
 * Junction type that can be distinguished based on Junction incoming and outgoing lanes number
 */
public enum JunctionType {

  /**
   * Junction that have only one incoming and only one outgoing lane
   */
  BEND,

  /**
   * Junction that have more than one incoming or/and more than one outgoing lane
   */
  CROSSROAD
}
