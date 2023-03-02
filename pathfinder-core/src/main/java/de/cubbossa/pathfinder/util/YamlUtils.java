package de.cubbossa.pathfinder.util;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

public class YamlUtils {

  private static final Map<Class<?>, Class<? extends SerializationWrapper<?>>> TYPES =
      new LinkedHashMapBuilder<Class<?>, Class<? extends SerializationWrapper<?>>>()
          .put(Particle.DustTransition.class, DustTransitionWrapper.class)
          .build();

  private YamlUtils() {
  }

  public static void registerClasses() {
    ConfigurationSerialization.registerClass(DustOptionsWrapper.class);
    ConfigurationSerialization.registerClass(DustTransitionWrapper.class);
  }

  public static <T> Object wrap(T any) {
    if (any == null) {
      return null;
    }
    Class<? extends SerializationWrapper<?>> wrapper = TYPES.get(any.getClass());
    if (wrapper == null) {
      return any;
    }
    try {
      return wrapper.getConstructors()[0].newInstance(any);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public static Object unwrap(Object wrapper) {
    return wrapper instanceof SerializationWrapper<?> w ? w.value() : wrapper;
  }

  /**
   * Some external classes don't implement {@link  org.bukkit.configuration.serialization.ConfigurationSerializable}.
   * To also be able to serialize these objects, SerializationWrappers are used.
   */
  public interface SerializationWrapper<T> extends ConfigurationSerializable {

    T value();
  }

  public record DustOptionsWrapper(Particle.DustOptions value)
      implements SerializationWrapper<Particle.DustOptions> {

    public static DustOptionsWrapper deserialize(Map<String, Object> values) {
      return new DustOptionsWrapper(new Particle.DustOptions(
          (Color) values.get("color"),
          ((Double) values.get("size")).floatValue()));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
      return new LinkedHashMapBuilder<String, Object>()
          .put("color", value.getColor())
          .put("size", value.getSize())
          .build();
    }
  }

  public record DustTransitionWrapper(
      Particle.DustTransition value) implements SerializationWrapper<Particle.DustTransition> {

    public static DustTransitionWrapper deserialize(Map<String, Object> values) {
      return new DustTransitionWrapper(new Particle.DustTransition(
          (Color) values.get("from"),
          (Color) values.get("to"),
          ((Double) values.get("size")).floatValue()));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
      return new LinkedHashMapBuilder<String, Object>()
          .put("from", value.getColor())
          .put("to", value.getToColor())
          .put("size", value.getSize())
          .build();
    }
  }

}
