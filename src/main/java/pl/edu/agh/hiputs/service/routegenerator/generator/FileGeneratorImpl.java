package pl.edu.agh.hiputs.service.routegenerator.generator;

import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.sql.Timestamp;

public class FileGeneratorImpl implements FileGenerator{

  @Override
  public String generateFileWithRoutes(Patch patch, Timestamp startTime, Timestamp endTime) {
//    TODO: na razie chyba generujemy losowe trasy do pliku
    return null;
  }
}
