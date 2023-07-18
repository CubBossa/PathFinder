package de.cubbossa.pathfinder.util;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class YamlUtils {

  private static final Map<Class<?>, Class<? extends SerializationWrapper<?>>> TYPES = Map.of(
      Vibration.class, VibrationWrapper.class,
      Vibration.Destination.class, VibrationDestinationWrapper.class,
      Vibration.Destination.BlockDestination.class, VibrationDestinationWrapper.class,
      Vibration.Destination.EntityDestination.class, VibrationDestinationWrapper.class,
      BlockData.class, BlockDataWrapper.class,
      Particle.DustTransition.class, DustTransitionWrapper.class,
      Particle.DustOptions.class, DustOptionsWrapper.class
  );

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

  public record BlockDataWrapper(BlockData value) implements SerializationWrapper<BlockData> {

    public static BlockDataWrapper deserialize(Map<String, Object> values) {
      if (!values.containsKey("data-string")) {
        throw new IllegalStateException("Cannot deserialize blockdata, missing attribute 'data-string'.");
      }
      return new BlockDataWrapper(Bukkit.createBlockData((String) values.get("data-string")));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
      return Map.of("data-string", value.getAsString());
    }
  }

  public record VibrationDestinationWrapper(
      Vibration.Destination value) implements SerializationWrapper<Vibration.Destination> {

    public static VibrationDestinationWrapper deserialize(Map<String, Object> values) {
      if (!values.containsKey("type") || !values.containsKey("dest")) {
        throw new IllegalStateException("Missing deserialization properties: 'type', 'dest'.");
      }
      if (values.get("type").equals("entity")) {
        Entity e = Bukkit.getEntity((UUID) values.get("dest"));
        return new VibrationDestinationWrapper(new Vibration.Destination.EntityDestination(e));
      } else {
        Location loc = (Location) values.get("dest");
        return new VibrationDestinationWrapper(new Vibration.Destination.BlockDestination(loc));
      }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
      if (value instanceof Vibration.Destination.EntityDestination e) {
        return Map.of(
            "type", "entity",
            "dest", e.getEntity().getEntityId()
        );
      }
      return Map.of(
          "type", "block",
          "dest", ((Vibration.Destination.BlockDestination) value).getLocation()
      );
    }
  }

  public record VibrationWrapper(Vibration value) implements SerializationWrapper<Vibration> {

    public static VibrationWrapper deserialize(Map<String, Object> values) {
      return new VibrationWrapper(new Vibration(
          (Location) values.get("location"),
          ((VibrationDestinationWrapper) values.get("destination")).value(),
          (Integer) values.get("arrival-time")
      ));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
      LinkedHashMap<String, Object> map = new LinkedHashMap<>();
      map.put("location", value.getOrigin());
      map.put("destination", new VibrationDestinationWrapper(value.getDestination()));
      map.put("arrival-time", value.getArrivalTime());
      return map;
    }
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
      return Map.of(
          "color", value.getColor(),
          "size", value.getSize()
      );
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
      return Map.of(
          "from", value.getColor(),
          "to", value.getToColor(),
          "size", value.getSize()
      );
    }
  }
}
