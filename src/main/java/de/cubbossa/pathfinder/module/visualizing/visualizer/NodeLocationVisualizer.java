package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.util.StringCompass;
import de.cubbossa.pathfinder.util.VectorUtils;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NodeLocationVisualizer implements PathVisualizer<NodeLocationVisualizer, NodeLocationVisualizer.Data> {

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
			edges.add(new Edge(index++, prev.getLocation().clone(), node.getLocation().clone()));
			prev = node;
		}

		BossBar bossBar = BossBar.bossBar(Component.text("abc"), 1f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_6);
		TranslationHandler.getInstance().getAudiences().player(player).showBossBar(bossBar);

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

		// find nearest edge

		Edge lastEdge = context.data().getLastGuessedEdge();
		Edge nearest = null;
		double edgeNearestDist = Double.MAX_VALUE;
		for (Edge edge : context.data().getEdges()) {
			double dist = VectorUtils.distancePointToSegment(
					targetPlayer.getEyeLocation().toVector(),
					edge.support().toVector(),
					edge.target().toVector());
			if (dist < edgeNearestDist) {
				nearest = edge;
				edgeNearestDist = dist;
			}
		}


		if (nearest == null) {
			throw new RuntimeException("The path does not contain any edges.");
		}

		// find the closest point on closest edge and move some blocks along in direction of target.
		Vector closestPoint = VectorUtils.closestPointOnSegment(
				targetPlayer.getEyeLocation().toVector(),
				nearest.support().toVector(),
				nearest.target().toVector()
		);

		// shift the closest point 5 units towards final target location
		double unitsToShift = 5;
		Location currentPoint = closestPoint.toLocation(targetPlayer.getWorld());
		Edge currentEdge = nearest;
		while (currentEdge != null && unitsToShift > 0) {
			double dist = currentPoint.distance(currentEdge.target());
			if (dist > unitsToShift) {
				currentPoint.add(currentEdge.target().clone().subtract(currentEdge.support()).toVector().normalize().multiply(unitsToShift));
				break;
			}
			unitsToShift -= dist;
			currentPoint = currentEdge.target().clone();
			currentEdge = currentEdge.index() + 1 >= context.data().getEdges().size()
					? null
					: context.data().getEdges().get(currentEdge.index() + 1);
		}

		targetPlayer.spawnParticle(Particle.FLAME, currentPoint, 1, 0, 0, 0, 0);
		ItemStack hand = targetPlayer.getInventory().getItemInMainHand();

		double angle = VectorUtils.convertDirectionToXZAngle(targetPlayer.getLocation());
		StringCompass compass = new StringCompass("<gray>" + "  |- · · · -+- · · · -|- · · · -+- · · · -| ".repeat(4), 20, () -> angle);
		compass.addMarker("N", "<red>N</red>", 0.);
		compass.addMarker("E", "E", 90.);
		compass.addMarker("S", "S", 180.);
		compass.addMarker("W", "W", 270.);
		double targetAngle = VectorUtils.convertDirectionToXZAngle(currentPoint.clone().subtract(targetPlayer.getLocation()).toVector());
		compass.addMarker("target", "<green>♦</green>", targetAngle);
		context.data().bossBar.name(compass.asComponent());
	}
}
