package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

public class NoLaneFoundException extends RuntimeException {

  public NoLaneFoundException(String patchId) {
    super(patchId);
  }
}
