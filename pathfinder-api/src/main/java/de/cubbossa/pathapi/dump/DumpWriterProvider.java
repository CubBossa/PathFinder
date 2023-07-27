package de.cubbossa.pathapi.dump;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class DumpWriterProvider {

  private static DumpWriter instance;

  @ApiStatus.Internal
  public static void set(DumpWriter instance) {
    DumpWriterProvider.instance = instance;
  }

  public static @NotNull DumpWriter get() {
    return instance;
  }
}