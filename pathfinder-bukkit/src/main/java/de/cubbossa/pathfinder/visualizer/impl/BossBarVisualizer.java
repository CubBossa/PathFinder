package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Getter
@Setter
public abstract class BossBarVisualizer<ViewT extends BossBarVisualizer<ViewT>.BossbarView>
    extends EdgeBasedVisualizer<ViewT> {

  public static final AbstractVisualizer.Property<CompassVisualizer, BossBar.Color> PROP_COLOR =
      new PropertyImpl<>("color", BossBar.Color.class,
          BossBarVisualizer::getColor, BossBarVisualizer::setColor);

  public static final AbstractVisualizer.Property<CompassVisualizer, BossBar.Overlay> PROP_OVERLAY =
      new PropertyImpl<>("overlay", BossBar.Overlay.class,
          BossBarVisualizer::getOverlay, BossBarVisualizer::setOverlay);

  private BossBar.Color color = BossBar.Color.GREEN;
  private BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
  private Double progress = 1.;

  public BossBarVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public ViewT createView(UpdatingPath nodes, PathPlayer<Player> player) {
    BossBar bossBar = BossBar.bossBar(Component.empty(), progress.floatValue(), color, overlay);
    return createView(player, nodes, bossBar);
  }

  public abstract ViewT createView(PathPlayer<Player> player, UpdatingPath nodes, BossBar bossBar);

  @Getter
  public abstract class BossbarView extends EdgeBasedView {

    private final BossBar bossBar;

    public BossbarView(PathPlayer<Player> player, UpdatingPath nodes, BossBar bossBar) {
      super(player, nodes);
      this.bossBar = bossBar;
    }

    @Override
    public void addViewer(PathPlayer<Player> player) {
      super.addViewer(player);
      PathFinder.get().getAudiences().player(player.unwrap().getUniqueId()).showBossBar(bossBar);
    }

    @Override
    public void removeViewer(PathPlayer<Player> player) {
      super.removeViewer(player);
      PathFinder.get().getAudiences().player(player.unwrap().getUniqueId()).hideBossBar(bossBar);
    }

    @Override
    public void removeAllViewers() {
      for (PathPlayer<Player> viewer : getViewers()) {
        PathFinder.get().getAudiences().player(viewer.unwrap().getUniqueId()).hideBossBar(bossBar);
      }
      super.removeAllViewers();
    }
  }
}
