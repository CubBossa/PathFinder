package de.cubbossa.pathfinder.dump;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.cubbossa.pathapi.dump.DumpWriter;
import de.cubbossa.pathapi.dump.DumpWriterProvider;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DumpWriterImpl implements DumpWriter {

  private final LinkedHashMap<String, Supplier<Object>> dumpMap;
  private final Gson gson;

  public DumpWriterImpl() {
    this(true);
  }

  public DumpWriterImpl(boolean prettyPrinting) {
    dumpMap = new LinkedHashMap<>();
    DumpWriterProvider.set(this);

    GsonBuilder builder = new GsonBuilder()
            .registerTypeAdapter(File.class, new TypeAdapter<File>() {
              @Override
              public void write(JsonWriter out, File value) throws IOException {
                out.value(value.getAbsolutePath());
              }

              @Override
              public File read(JsonReader in) throws IOException {
                return new File(in.nextString());
              }
            })
            .registerTypeAdapter(Color.class, new TypeAdapter<Color>() {
              @Override
              public void write(JsonWriter out, Color value) throws IOException {
                out.value(Integer.toHexString(value.getRGB()));
              }

              @Override
              public Color read(JsonReader in) throws IOException {
                return null;
              }
            });
    if (prettyPrinting) {
      builder.setPrettyPrinting();
    }
    gson = builder.create();
  }

  @Override
  public void addProperty(String name, Object data) {
    addProperty(name, () -> data);
  }

  @Override
  public void addProperty(String name, Supplier<Object> data) {
    if (dumpMap.containsKey(name)) {
      throw new IllegalArgumentException("Another dump value with the key '" + name + "' has already been registered.");
    }
    dumpMap.put(name, data);
  }

  @Override
  public boolean removeProperty(String name) {
    return dumpMap.remove(name) != null;
  }

  @Override
  public String toString() {
    return gson.toJson(resolve());
  }

  @Override
  public void save(File file) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      gson.toJson(resolve(), writer);
    }
  }

  private Map<String, Object> resolve() {
    Map<String, Object> resolved = new LinkedHashMap<>();
    dumpMap.forEach((s, objectSupplier) -> {
      resolved.put(s, objectSupplier.get());
    });
    return resolved;
  }
}
