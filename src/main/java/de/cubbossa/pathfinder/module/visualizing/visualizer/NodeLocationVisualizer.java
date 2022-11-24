package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.util.VectorUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NodeLocationVisualizer implements PathVisualizer<NodeLocationVisualizer, NodeLocationVisualizer.Data> {

	private static final String COMPASS = "----:---- N ----:---- E ----:---- W ----:---- S ";

	private final NamespacedKey key = new NamespacedKey(PathPlugin.getInstance(), "abc");
	private int interval = 10;
	private String nameFormat = "name";
	private Component displayName = Component.text("name");
	private @Nullable String permission;

	@Override
	public VisualizerType<NodeLocationVisualizer> getType() {
		return VisualizerHandler.COMPASS_VISUALIZER_TYPE;
	}

	private record Edge(int index, Location support, Location target) {
	}

	@Getter
	@RequiredArgsConstructor
	public static class Data {
		private final List<Node> nodes;
		private final List<Edge> edges;
		private Location lastPlayerLocation;
		private Edge lastGuessedEdge;
		private final BossBar bossBar;

		List<Node> getNodes(Player player) {
			return nodes;
		}
	}

	@Override
	public Data prepare(List<Node> nodes, Player player) {

		List<Edge> edges = new ArrayList<>();
		Node prev = null;
		int index = 0;
		for (Node node : nodes) {
			if (prev == null) {
				prev = node;
				continue;
			}
			edges.add(new Edge(index++, prev.getLocation(), node.getLocation()));
			prev = node;
		}

		BossBar bossBar = BossBar.bossBar(Component.text("abc"), 1f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_6);
		return new Data(nodes, edges, bossBar);
	}

	@Override
	public void play(VisualizerContext<Data> context) {
		Player targetPlayer = context.player();

		// No need to update, the player has not moved.
		if (targetPlayer.getLocation().equals(context.data().getLastPlayerLocation())) {
			return;
		}
		context.data().lastPlayerLocation = targetPlayer.getLocation();

		Edge lastEdge = context.data().getLastGuessedEdge();
		Edge nearest = null;
		double edgeNearestDist = Double.MAX_VALUE;
		for (Edge edge : context.data().getEdges()) {
			double dist = VectorUtils.distancePointToLine(
					targetPlayer.getLocation().toVector(),
					edge.support().toVector(),
					edge.target().toVector());
			if (edge.equals(lastEdge)) {
				dist *= .5f;
			}
			if (edge.index > lastEdge.index) {
				dist -= 5;
			}
			if (dist < edgeNearestDist) {
				nearest = edge;
				edgeNearestDist = dist;
			}
		}

		System.out.println(nearest);
		Location targetLocation = nearest.target();
		ItemStack compass = targetPlayer.getInventory().getItemInMainHand();
		if (compass.getItemMeta() instanceof CompassMeta meta) {
			meta.setLodestoneTracked(false);
			meta.setLodestone(targetLocation);
			compass.setItemMeta(meta);
		}
	}
}
