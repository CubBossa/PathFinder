package de.cubbossa.pathfinder.editmode;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.auto.service.AutoService;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.jetbrains.annotations.NotNull;

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
  public void onLoad(PathFinder pathPlugin) {
    PathFinderExtension.super.onLoad(pathPlugin);
    if (pathPlugin instanceof BukkitPathFinder bukkitPathFinder) {
      PacketEvents.setAPI(SpigotPacketEventsBuilder.build(bukkitPathFinder.getJavaPlugin()));
      PacketEvents.getAPI().getSettings()
          .checkForUpdates(true)
          .bStats(true);
      PacketEvents.getAPI().load();
    }
  }

  @Override
  public void onEnable(PathFinder plugin) {

    PacketEvents.getAPI().init();

    PathFinderProvider.get().getLogger().info("Enabling default roadmap editors.");

    new GUIHandler(PathFinderPlugin.getInstance());
    GUIHandler.getInstance().enable();

    PathFinderProvider.get().getLogger().info("Successfully enabled default roadmap editors.");
  }

  @Override
  public void onDisable(PathFinder plugin) {
    PathFinderProvider.get().getLogger().info("Disabling default roadmap editors.");
    GUIHandler.getInstance().disable();

    PacketEvents.getAPI().terminate();
  }
}
