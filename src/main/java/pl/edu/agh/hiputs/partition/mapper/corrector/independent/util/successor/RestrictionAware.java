package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor;

import java.util.Set;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

public interface RestrictionAware {

  void provideRestrictions(Set<Restriction> restrictions);

}
