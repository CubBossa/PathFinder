package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.translations.TranslationHandler;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@Getter
@Setter
public abstract class BossBarVisualizer<T extends BossBarVisualizer<T, D>, D extends BossBarVisualizer.Data>
    extends EdgeBasedVisualizer<T, D> {

  public static final Property<CompassVisualizer, BossBar.Color> PROP_COLOR =
      new Property.SimpleProperty<>("color", BossBar.Color.class, true,
          BossBarVisualizer::getColor, BossBarVisualizer::setColor);

  public static final Property<CompassVisualizer, BossBar.Overlay> PROP_OVERLAY =
      new Property.SimpleProperty<>("overlay", BossBar.Overlay.class, true,
          BossBarVisualizer::getOverlay, BossBarVisualizer::setOverlay);

  private BossBar.Color color = BossBar.Color.GREEN;
  private BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
  private Double progress = 1.;

  public BossBarVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public D newData(Player player, List<Node<?>> nodes, List<Edge> edges) {
    BossBar bossBar = BossBar.bossBar(Component.empty(), progress.floatValue(), color, overlay);
    TranslationHandler.getInstance().getAudiences().player(player).showBossBar(bossBar);
    return newData(player, nodes, edges, bossBar);
  }

  public abstract D newData(Player player, List<Node<?>> nodes, List<Edge> edges, BossBar bossBar);

  @Override
  public void destruct(Player player, D data) {
    super.destruct(player, data);
    TranslationHandler.getInstance().getAudiences().player(player).hideBossBar(data.getBossBar());
  }

  @Getter
  public static class Data extends EdgeBasedVisualizer.Data {
    private final BossBar bossBar;

    public Data(List<Node<?>> nodes, List<Edge> edges, BossBar bossBar) {
      super(nodes, edges);
      this.bossBar = bossBar;
    }
  }
}
