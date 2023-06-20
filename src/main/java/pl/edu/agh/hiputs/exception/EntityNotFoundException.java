package pl.edu.agh.hiputs.exception;

import lombok.AllArgsConstructor;

import static java.text.MessageFormat.format;

@AllArgsConstructor
public class EntityNotFoundException extends Exception {

  private final String entityName;
  private final String dateSourceName;

  @Override
  public String getMessage() {
    return format("Entity {0} not found in {1}", entityName, dateSourceName);
  }


}
