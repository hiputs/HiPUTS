package pl.edu.agh.hiputs.utils;

import pl.edu.agh.hiputs.exception.EntityNotFoundException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CollectionUtil {

  public static <KEY, VALUE> VALUE getOrThrow(Map<KEY, VALUE> map, KEY key) throws EntityNotFoundException {
    return Optional.ofNullable(map.get(key)).orElseThrow(
      () -> new EntityNotFoundException(key.getClass().getName(), map.getClass().getName())
    );
  }

  public static <T> Optional<T> optionalWhen(boolean condition, Supplier<T> function) {
    return condition ? Optional.of(function.get()) : Optional.empty();
  }
}
