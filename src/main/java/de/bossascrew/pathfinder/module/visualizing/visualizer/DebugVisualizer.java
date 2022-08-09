package de.bossascrew.pathfinder.module.visualizing.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@Getter
@Setter
public class DebugVisualizer extends Visualizer {

	private double tangentLength;
	private int tickDelay;

	public DebugVisualizer(NamespacedKey key) {
		super(key, "<red>Debug</red>");
		this.tangentLength = 0;
		this.tickDelay = 10;
	}

	@Override
	public void playParticle(Player player, Location location, int index, long time) {
		player.spawnParticle(Particle.FLAME, location, 0);
	}
}
