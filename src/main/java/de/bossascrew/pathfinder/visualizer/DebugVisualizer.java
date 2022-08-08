package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class DebugVisualizer implements PathVisualizer {

	private final NamespacedKey key;
	private String nameFormat;
	private Component displayName;
	private @Nullable String permission;
	private double tangentLength;
	private int tickDelay;

	public DebugVisualizer(NamespacedKey key) {
		this.key = key;
		setNameFormat("<red>Debug</red>");
		this.permission = null;
		this.tangentLength = 0;
		this.tickDelay = 10;
	}

	public void setNameFormat(String name) {
		this.nameFormat = name;
		this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(name);
	}

	@Override
	public void playParticle(Player player, Location location, int index, long time) {
		player.spawnParticle(Particle.FLAME, location, 0);
	}
}
