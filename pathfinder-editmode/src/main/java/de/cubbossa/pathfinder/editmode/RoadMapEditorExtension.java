package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PathPluginExtension;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.jetbrains.annotations.NotNull;

@AutoService(PathPluginExtension.class)
public class RoadMapEditorExtension implements PathPluginExtension {

  private static final NamespacedKey KEY = new NamespacedKey(PathPlugin.getInstance(), "rmeditors");

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+");
  private static final Pattern BUILD_PATTERN = Pattern.compile("b([0-9]+)");

  @Override
  public void onEnable() {

    Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
    if (protocolLib == null) {
      throw new IllegalStateException("Cannot use editmode without ProtocolLib");
    }
    String spigotVersion = Bukkit.getVersion();
    String protocolLibVersion = protocolLib.getDescription().getVersion();

    Matcher spigotMatcher = VERSION_PATTERN.matcher(spigotVersion);
    String spigotVersionExtracted = spigotMatcher.find() ? spigotMatcher.group(0) : "";
    Matcher plVersionMatcher = VERSION_PATTERN.matcher(protocolLibVersion);
    String protocolLibVersionExtracted = plVersionMatcher.find() ? plVersionMatcher.group(0) : "";
    Matcher plBuildMatcher = BUILD_PATTERN.matcher(protocolLibVersion);
    String protocolLibBuildExtracted = plBuildMatcher.find() ? plBuildMatcher.group(1) : "0";

    if (spigotVersionExtracted.startsWith("1.19")) {
      // require 5.0.0 up to build 606
      if (!protocolLibVersionExtracted.startsWith("5.")) {
        throw new UnknownDependencyException("Invalid ProtocolLib version. Use at least ProtocolLib v5.0.0 for Minecraft 1.19");
      }
      if (spigotVersionExtracted.equalsIgnoreCase("1.19.3")) {
        // search for build 607+
        if (Integer.parseInt(protocolLibBuildExtracted) < 607) {
          throw new UnknownDependencyException("Invalid ProtocolLib version. Use at least ProtocolLib v5.0.0-SNAPSHOT-b607 for Minecraft 1.19.3.");
        }
      } else {
        if (Integer.parseInt(protocolLibBuildExtracted) > 606) {
          throw new UnknownDependencyException("Invalid ProtocolLib version. Use at latest ProtocolLib v5.0.0-SNAPSHOT-b606 for Minecraft 1.19.2.");
        }
      }
    }
    // else require below 5.0.0

    PathPlugin.getInstance().getLogger().info("Enabling default roadmap editors.");

    new GUIHandler(PathPlugin.getInstance());
    GUIHandler.getInstance().enable();

    PathPlugin.getInstance().getLogger().info("Successfully enabled default roadmap editors.");
  }

  @Override
  public void onDisable() {
    PathPlugin.getInstance().getLogger().info("Disabling default roadmap editors.");
    GUIHandler.getInstance().disable();
  }
}
