package de.cubbossa.pathfinder.editmode;

import com.github.retrooper.packetevents.PacketEvents;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.PathFinderPlugin;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

@Extension(points = PathFinderExtension.class)
public class RoadMapEditorExtension extends PathFinderExtensionBase implements PathFinderExtension {

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return AbstractPathFinder.pathfinder("group-editor");
  }

  @Override
  public void onLoad(PathFinder pathPlugin) {
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

    new GUIHandler(PathFinderPlugin.getInstance());
    GUIHandler.getInstance().enable();
  }

  @Override
  public void onDisable(PathFinder plugin) {
    GUIHandler.getInstance().disable();

    PacketEvents.getAPI().terminate();
  }
}
