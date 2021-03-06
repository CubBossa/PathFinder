package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.Named;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

@Getter
@Setter
@RequiredArgsConstructor
public class NodeType<T extends Node> implements Keyed, Named {

	private final NamespacedKey key;
	private String nameFormat;
	private Component displayName;
	private final ItemStack displayItem;
	private final BiFunction<RoadMap, Integer, T> factory;

	public NodeType(NamespacedKey key, String name, ItemStack displayItem, BiFunction<RoadMap, Integer, T> factory) {
		this.key = key;
		this.setNameFormat(name);
		this.displayItem = displayItem;
		this.factory = factory;
	}

	@Override
	public void setNameFormat(String name) {
		this.nameFormat = name;
		this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(name);
	}
}
