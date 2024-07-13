package de.cubbossa.pathfinder.module.quests;

import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public class PathFinderQuestsModule extends JavaPlugin {

  @Override
  public void onEnable() {
    getLogger().log(Level.SEVERE, "PathFinderQuestsModule is not a plugin, it is a module of the Quests plugin" +
        "and must be installed in the modules directory of the Quests directory (/plugins/quests/modules/...)");
    getPluginLoader().disablePlugin(this);
  }
}