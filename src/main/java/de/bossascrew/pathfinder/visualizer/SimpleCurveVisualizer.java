package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.events.internal.SimpleCurveVisualizerUpdate;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.util.GameAction;
import de.bossascrew.splinelib.util.Spline;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
@Setter
public class SimpleCurveVisualizer implements Keyed, PathVisualizer {

	private final NamespacedKey key;
	private String nameFormat;
	private Component dislayName;

	@Nullable
	private String permission = null;
	private Material iconType = Material.NAME_TAG;

	private int particleSteps = 1;
	private Particle particle = Particle.REDSTONE;
	private Double particleDistance = .2;
	private Integer schedulerPeriod = 10;
	private double tangentLength = 3;

	protected final GameAction<SimpleCurveVisualizerUpdate> updateParticle;

	public SimpleCurveVisualizer(NamespacedKey key, String nameFormat) {
		this.key = key;
		this.nameFormat = nameFormat;
		updateParticle = new GameAction<>();
	}

	@Override
	public void playParticle(Player player, Location location, int index, int time) {
		if (index % particleSteps == time % particleSteps) {
			player.spawnParticle(particle, location, 1);
		}
	}

	@Override
	public Spline makeSpline(List<Node> nodes) {
		return PathVisualizer.super.makeSpline(nodes);
	}
}
