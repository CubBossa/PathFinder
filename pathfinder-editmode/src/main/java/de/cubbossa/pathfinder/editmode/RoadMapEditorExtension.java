package de.cubbossa.pathfinder.editmode;

import de.cubbossa.cliententities.lib.packetevents.api.PacketEvents;
import de.cubbossa.cliententities.lib.packetevents.impl.factory.spigot.SpigotPacketEventsBuilder;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.misc.NamespacedKey;
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
