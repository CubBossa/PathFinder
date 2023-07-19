package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoService(PathFinderExtension.class)
public class RoadMapEditorExtension implements PathFinderExtension {

  private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+");
  private static final Pattern BUILD_PATTERN = Pattern.compile("b([0-9]+)");

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return CommonPathFinder.pathfinder("group-editor");
  }

  @Override
  public void onEnable(PathFinder plugin) {

    Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
    if (protocolLib == null) {
      throw new IllegalStateException("Cannot use editmode without ProtocolLib");
    }
    String spigotVersion = Bukkit.getVersion();

    Matcher spigotMatcher = VERSION_PATTERN.matcher(spigotVersion);
    String spigotVersionExtracted = spigotMatcher.find() ? spigotMatcher.group(0) : "";

    checkVersion(spigotVersionExtracted, protocolLib.getDescription().getVersion());

    PathFinderProvider.get().getLogger().info("Enabling default roadmap editors.");

    new GUIHandler(PathFinderPlugin.getInstance());
    GUIHandler.getInstance().enable();

    PathFinderProvider.get().getLogger().info("Successfully enabled default roadmap editors.");
  }

  @Override
  public void onDisable(PathFinder plugin) {
    PathFinderProvider.get().getLogger().info("Disabling default roadmap editors.");
    GUIHandler.getInstance().disable();
  }

  public void checkVersion(String spigotVersion, String protocolLibVersion) {
    Version protocolLib = new Version(protocolLibVersion);
    Version spigot = new Version(spigotVersion);

    if (spigot.compareTo(new Version("1.19")) >= 0) {
      // newest protocollib, good to go with all 1.19
      if (protocolLib.compareTo(new Version("5.0.0")) >= 0) {
        return;
      }

      // require 5.0.0 up to build 606
      if (protocolLib.compareTo(new Version("5")) < 0) {
        throw new UnknownDependencyException(
            "Invalid ProtocolLib version. Use at least ProtocolLib v5.0.0 for Minecraft 1.19"
                + " Current ProtocolLib: " + new Version(protocolLibVersion));
      }
      if (spigot.compareTo(new Version("1.19.3")) >= 0) {
        // search for build 607+
        if (protocolLib.compareTo(new Version("v5.0.0-SNAPSHOT-b607")) < 0) {
          throw new UnknownDependencyException(
              "Invalid ProtocolLib version. Use at least ProtocolLib v5.0.0-SNAPSHOT-b607 for Minecraft 1.19.3."
                  + " Current ProtocolLib: " + new Version(protocolLibVersion));
        }
      } else {
        if (protocolLib.compareTo(new Version("v5.0.0-SNAPSHOT-b606")) > 0) {
          throw new UnknownDependencyException(
              "Invalid ProtocolLib version. Use at latest ProtocolLib v5.0.0-SNAPSHOT-b606 for Minecraft 1.19.2."
                  + " Current ProtocolLib: " + new Version(protocolLibVersion));
        }
      }
    }
    // else require below 5.0.0
  }
}
