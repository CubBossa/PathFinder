package de.cubbossa.pathfinder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public class BStatsLoader {

  public void registerStatistics(JavaPlugin plugin) {
    Metrics metrics = new Metrics(plugin, 16324);

    metrics.addCustomChart(new SimplePie("group_amount",
        () -> PathPlugin.getInstance().getStorage().loadAllGroups().join().size() + ""));
    metrics.addCustomChart(new SimplePie("visualizer_amount",
        () -> PathPlugin.getInstance().getStorage().loadVisualizers().join().size() + ""));
    metrics.addCustomChart(new AdvancedPie("nodes_per_group", () -> {
      IntStream counts = PathPlugin.getInstance().getStorage().loadAllGroups().join().stream()
          .mapToInt(Collection::size);
      Map<String, Integer> vals = new HashMap<>();
      counts.forEach(value -> {
        if (value < 10) {
          vals.put("< 10", vals.getOrDefault("< 10", 0) + 1);
        } else if (value < 30) {
          vals.put("10-30", vals.getOrDefault("10-30", 0) + 1);
        } else if (value < 50) {
          vals.put("30-50", vals.getOrDefault("30-50", 0) + 1);
        } else if (value < 100) {
          vals.put("50-100", vals.getOrDefault("50-100", 0) + 1);
        } else if (value < 150) {
          vals.put("100-150", vals.getOrDefault("100-150", 0) + 1);
        } else if (value < 200) {
          vals.put("150-200", vals.getOrDefault("150-200", 0) + 1);
        } else if (value < 300) {
          vals.put("200-300", vals.getOrDefault("200-300", 0) + 1);
        } else if (value < 500) {
          vals.put("300-500", vals.getOrDefault("300-500", 0) + 1);
        } else {
          vals.put("> 500", vals.getOrDefault("> 500", 0) + 1);
        }
      });
      return vals;
    }));
  }
}
