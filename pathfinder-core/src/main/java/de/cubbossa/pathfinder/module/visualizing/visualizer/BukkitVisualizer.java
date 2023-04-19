package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.module.visualizing.AbstractVisualizer;
import org.bukkit.entity.Player;

public abstract class BukkitVisualizer<T extends PathVisualizer<T, D, Player>, D>
		extends AbstractVisualizer<T, D, Player> {

	public BukkitVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}

	@Override
	public Class<Player> getTargetType() {
		return Player.class;
	}

	@Override
	public void destruct(PathPlayer<Player> player, D data) {
	}
}
