package pl.edu.agh.hiputs.utils;

import java.lang.reflect.Field;

public final class ReflectionUtil {

  private ReflectionUtil() {
  }

  public static void setFieldValue(Object object, String fieldName, Object value) {
    try {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(object, value);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(
          "Reflection in tests setting for class: " + object.getClass() + ", field: " + fieldName + ", value: "
              + value);
    }
  }

  public static Object getFieldValue(Object object, String fieldName) {
    try {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(object);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(
          "Reflection in tests getting from class: " + object.getClass() + ", field: " + fieldName);
    }
  }
}
