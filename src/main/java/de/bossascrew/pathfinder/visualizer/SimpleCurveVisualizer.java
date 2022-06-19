package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
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
	private Component displayName;

	@Nullable
	private String permission = null;
	private Material iconType = Material.NAME_TAG;

	private int particleSteps = 1;
	private Particle particle = Particle.REDSTONE;
	private Double particleDistance = .2;
	private Integer schedulerPeriod = 10;
	private double tangentLength = 3;

	public SimpleCurveVisualizer(NamespacedKey key, String nameFormat) {
		this.key = key;
		setNameFormat(nameFormat);
	}

	public void setNameFormat(String nameFormat) {
		this.nameFormat = nameFormat;
		this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
	}

	@Override
	public void playParticle(Player player, Location location, int index, long time) {
		if (index % particleSteps == time % particleSteps) {
			player.spawnParticle(particle, location, 1);
		}
	}

	@Override
	public int getTickDelay() {
		return schedulerPeriod;
	}

	@Override
	public Spline makeSpline(List<Node> nodes) {
		return PathVisualizer.super.makeSpline(nodes);
	}
}
