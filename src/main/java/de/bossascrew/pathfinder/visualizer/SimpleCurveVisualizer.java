package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.Node;
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
public class SimpleCurveVisualizer implements Keyed, PathVisualizer {

	private final NamespacedKey key;
	private String nameFormat;
	private Component displayName;

	@Nullable
	private String permission = null;
	private ItemStack displayItem = new ItemStack(Material.REDSTONE);

	private int particleSteps = 10;
	private ParticleBuilder particle = new ParticleBuilder(ParticleEffect.BLOCK_DUST)
			.setAmount(1)
			.setSpeed(0)
			.setColor(java.awt.Color.GREEN);
	private Double particleDistance = .2;
	private Integer schedulerPeriod = 2;
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
		if (index % particleSteps == time/schedulerPeriod % particleSteps) {
			particle.setLocation(location).display(player);
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
