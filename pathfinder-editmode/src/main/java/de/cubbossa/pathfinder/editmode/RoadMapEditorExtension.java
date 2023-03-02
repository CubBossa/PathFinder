package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PathPluginExtension;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

@AutoService(PathPluginExtension.class)
public class RoadMapEditorExtension implements PathPluginExtension {

  private static final NamespacedKey KEY = new NamespacedKey(PathPlugin.getInstance(), "rmeditors");

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  @Override
  public void onEnable() {
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
