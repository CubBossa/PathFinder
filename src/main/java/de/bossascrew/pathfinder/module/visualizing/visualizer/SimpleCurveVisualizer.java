package de.bossascrew.pathfinder.module.visualizing.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.node.Node;
import de.bossascrew.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.bossascrew.splinelib.util.Spline;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
@Setter
public class SimpleCurveVisualizer extends ParticleVisualizer {

	private int particleSteps = 10;
	private ParticleBuilder particle = new ParticleBuilder(ParticleEffect.BLOCK_DUST)
			.setAmount(1)
			.setSpeed(0)
			.setColor(java.awt.Color.GREEN);
	private Double particleDistance = .2;
	private Integer schedulerPeriod = 2;
	private double tangentLength = 3;

	public SimpleCurveVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}


	@Override
	public void playParticle(Player player, Location location, int index, long time) {
		if (index % particleSteps == time/schedulerPeriod % particleSteps) {
			particle.setLocation(location).display(player);
		}
	}

	@Override
	public int getTickDelay() {
		return schedulerPeriod;
	}
}
