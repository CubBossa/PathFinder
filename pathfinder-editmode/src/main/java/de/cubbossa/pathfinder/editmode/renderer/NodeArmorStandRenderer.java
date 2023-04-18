package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathfinder.api.editor.GraphRenderer;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.editmode.utils.ItemStackUtils;
import de.cubbossa.pathfinder.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class NodeArmorStandRenderer extends AbstractArmorstandRenderer<Node<?>> implements GraphRenderer<Player> {

	public static final Action<TargetContext<Node<?>>> RIGHT_CLICK_NODE = new Action<>();
	public static final Action<TargetContext<Node<?>>> LEFT_CLICK_NODE = new Action<>();
	private static final Vector NODE_OFFSET = new Vector(0, -1.75, 0);

	private final ItemStack nodeHead = ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_GREEN);

	public NodeArmorStandRenderer(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	Location retrieveFrom(Node<?> element) {
		return VectorUtils.toBukkit(element.getLocation()).add(NODE_OFFSET);
	}

	@Override
	Action<TargetContext<Node<?>>> handleInteract(Player player, int slot, boolean left) {
		return left ? RIGHT_CLICK_NODE : LEFT_CLICK_NODE;
	}

	@Override
	ItemStack head(Node<?> element) {
		return nodeHead.clone();
	}

	@Override
	public CompletableFuture<Void> clear(PathPlayer<Player> player) {
		hideElements(entityNodeMap.values(), player.unwrap());
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {
		super.showElements(nodes, player.unwrap());
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {
		return CompletableFuture.completedFuture(null);
	}
}
