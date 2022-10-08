package pl.edu.agh.hiputs.utils;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Service
@RequiredArgsConstructor
public class DebugUtils {

  private final MapRepository mapRepository;

  private static final List<Object> objects = new LinkedList<>();

  @PostConstruct
  private void init(){
    objects.add(mapRepository);
  }

  public void setMapFragment(MapFragment mapFragment){
    objects.add(mapFragment);
  }

  public static MapRepository getMapRepository(){
    return (MapRepository) objects.get(0);
  }

  public static MapFragment getMapFragment(){
    return (MapFragment) objects.get(1);
  }

}
