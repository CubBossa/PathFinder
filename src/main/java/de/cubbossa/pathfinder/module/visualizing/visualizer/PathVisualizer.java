package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PermissionHolder;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface PathVisualizer<T extends PathVisualizer<T, D>, D> extends Keyed, Named, PermissionHolder {

	VisualizerType<T> getType();

	D prepare(List<Node> nodes, Player player);

	void play(VisualizerContext<D> context);

	record VisualizerContext<D>(List<Player> players, int interval, long time, D data) {

		public Player player() {
			return players.get(0);
		}

	}

	int getInterval();

	void setInterval(int interval);
}
