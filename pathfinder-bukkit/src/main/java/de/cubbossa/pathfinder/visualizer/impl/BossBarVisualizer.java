package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Setter
public abstract class BossBarVisualizer<ViewT extends BossBarVisualizer<ViewT>.BossbarView>
    extends EdgeBasedVisualizer<ViewT> {

  public static final AbstractVisualizer.Property<CompassVisualizer, BossBar.Color> PROP_COLOR =
      new AbstractVisualizer.SimpleProperty<>("color", BossBar.Color.class,
          BossBarVisualizer::getColor, BossBarVisualizer::setColor);

  public static final AbstractVisualizer.Property<CompassVisualizer, BossBar.Overlay> PROP_OVERLAY =
      new AbstractVisualizer.SimpleProperty<>("overlay", BossBar.Overlay.class,
          BossBarVisualizer::getOverlay, BossBarVisualizer::setOverlay);

  private BossBar.Color color = BossBar.Color.GREEN;
  private BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
  private Double progress = 1.;

  public BossBarVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public ViewT createView(List<Node> nodes, List<Edge> edges, PathPlayer<Player> player) {
    BossBar bossBar = BossBar.bossBar(Component.empty(), progress.floatValue(), color, overlay);
    return createView(player, nodes, edges, bossBar);
  }

  public abstract ViewT createView(PathPlayer<Player> player, List<Node> nodes, List<Edge> edges, BossBar bossBar);

  @Getter
  public abstract class BossbarView extends EdgeBasedView {

    private final BossBar bossBar;

    public BossbarView(PathPlayer<Player> player, List<Node> nodes, List<Edge> edges, BossBar bossBar) {
      super(player, nodes, edges);
      this.bossBar = bossBar;
    }

    @Override
    public void addViewer(PathPlayer<Player> player) {
      super.addViewer(player);
      PathFinderProvider.get().getAudiences().player(player.unwrap().getUniqueId()).showBossBar(bossBar);
    }

    @Override
    public void removeViewer(PathPlayer<Player> player) {
      super.removeViewer(player);
      PathFinderProvider.get().getAudiences().player(player.unwrap().getUniqueId()).hideBossBar(bossBar);
    }
  }
}
