package pl.edu.agh.hiputs.utils;

import pl.edu.agh.hiputs.exception.EntityNotFoundException;

import java.util.Map;
import java.util.Optional;

public class CollectionUtil {

  public static <K, V> V getOrThrow(Map<K, V> map, K key) throws EntityNotFoundException {
    return Optional.ofNullable(map.get(key)).orElseThrow(
      () -> new EntityNotFoundException(key.getClass().getName(), map.getClass().getName())
    );
  }

}
