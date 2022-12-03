package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Setter
public abstract class BossBarVisualizer<T extends BossBarVisualizer<T, D>, D extends BossBarVisualizer.Data> extends EdgeBasedVisualizer<T, D> {

	private BossBar.Color color;
	private BossBar.Overlay overlay;
	private Double progress;

	@Getter
	public static class Data extends EdgeBasedVisualizer.Data {
		private final BossBar bossBar;

		public Data(List<Node> nodes, List<Edge> edges, BossBar bossBar) {
			super(nodes, edges);
			this.bossBar = bossBar;
		}
	}

	public BossBarVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}

	@Override
	public D newData(Player player, List<Node> nodes, List<Edge> edges) {
		BossBar bossBar = BossBar.bossBar(Component.empty(), progress.floatValue(), color, overlay);
		TranslationHandler.getInstance().getAudiences().player(player).showBossBar(bossBar);
		return newData(player, nodes, edges, bossBar);
	}

	public abstract D newData(Player player, List<Node> nodes, List<Edge> edges, BossBar bossBar);

	@Override
	public void destruct(Player player, D data) {
		super.destruct(player, data);
		TranslationHandler.getInstance().getAudiences().player(player).hideBossBar(data.getBossBar());
	}
}
