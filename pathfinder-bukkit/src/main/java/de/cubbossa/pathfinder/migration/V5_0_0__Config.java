package de.cubbossa.pathfinder.migration;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderProvider;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V5_0_0__Config extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws Exception {

    PathFinder pathFinder = PathFinderProvider.get();

    File config = new File(pathFinder.getDataFolder(), "config.yml");
    if (!config.exists()) {
      return;
    }
    YamlConfiguration yml = YamlConfiguration.loadConfiguration(config);
    yml.set("version", null);
    yml.save(config);
  }
}
