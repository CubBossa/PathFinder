package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.InternalVisualizerDataStorage;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public abstract class Visualizer<T extends PathVisualizer<T, D>, D>
    implements PathVisualizer<T, D> {

  private final NamespacedKey key;
  private String nameFormat;
  private Component displayName;

  @Nullable
  private String permission = null;
  private ItemStack displayItem = new ItemStack(Material.REDSTONE);
  private int interval = 1;

  public Visualizer(NamespacedKey key, String nameFormat) {
    this.key = key;
    setNameFormat(nameFormat);
  }

  public void setNameFormat(String nameFormat) {
    this.nameFormat = nameFormat;
    this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
  }

}
