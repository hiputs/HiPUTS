package pl.edu.agh.hiputs.partition.mapper.helper.structure.complex;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.springframework.stereotype.Repository;

@Repository
public class ComplexCrossroadsRepositoryImpl implements ComplexCrossroadsRepository {

  @Getter   // implementing done by getter
  private final Set<ComplexCrossroad> complexCrossroads = new HashSet<>();
}

