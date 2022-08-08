package de.bossascrew.pathfinder.roadmap;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.PathPlayerHandler;
import de.bossascrew.pathfinder.events.node.*;
import de.bossascrew.pathfinder.events.nodegroup.NodeGroupAssignEvent;
import de.bossascrew.pathfinder.menu.EditModeMenu;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.util.ClientNodeHandler;
import de.bossascrew.pathfinder.util.LerpUtils;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

import java.awt.Color;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class RoadMapEditor implements Keyed, Listener {

	private final NamespacedKey key;
	private final RoadMap roadMap;
	private final ClientNodeHandler armorstandHandler;

	private final Map<UUID, BottomInventoryMenu> editingPlayers;
	private final Map<UUID, GameMode> preservedGameModes;

	private final Collection<Integer> editModeTasks;

	private float particleDistance = .3f;
	private int tickDelay = 5;
	private Color colorFrom = new Color(255, 0, 0);
	private Color colorTo = new Color(0, 127, 255);

	public RoadMapEditor(RoadMap roadMap) {
		this.key = roadMap.getKey();
		this.roadMap = roadMap;

		this.editModeTasks = new HashSet<>();
		this.armorstandHandler = new ClientNodeHandler(PathPlugin.getInstance());
		this.editingPlayers = new HashMap<>();
		this.preservedGameModes = new HashMap<>();

		Bukkit.getPluginManager().registerEvents(this, PathPlugin.getInstance());
	}

	public void dispose() {
		HandlerList.unregisterAll(this);
	}

	public boolean isEdited() {
		return !editingPlayers.isEmpty();
	}

	public void toggleEditMode(UUID uuid) {
		setEditMode(uuid, !isEditing(uuid));
	}

	public void cancelEditModes() {
		for (UUID uuid : editingPlayers.keySet()) {
			setEditMode(uuid, false);
		}
	}

	/**
	 * Sets a player into edit mode for this roadmap.
	 *
	 * @param uuid     the player to set the edit mode for
	 * @param activate activate or deactivate edit mode
	 */
	public void setEditMode(UUID uuid, boolean activate) {
		Player player = Bukkit.getPlayer(uuid);
		PathPlayer editor = PathPlayerHandler.getInstance().getPlayer(uuid);
		if (editor == null) {
			return;
		}

		if (activate) {
			if (player == null) {
				return;
			}
			if (!isEdited()) {
				startParticleTask();
			}
			editor.setEditMode(key);

			BottomInventoryMenu menu = new EditModeMenu(roadMap, NodeTypeHandler.getInstance().getTypes().values()).createHotbarMenu(this);
			editingPlayers.put(uuid, menu);
			menu.openSync(player);

			preservedGameModes.put(player.getUniqueId(), player.getGameMode());
			player.setGameMode(GameMode.CREATIVE);

			showArmorStands(player);
		} else {

			if (player != null) {
				BottomInventoryMenu menu = editingPlayers.get(uuid);
				if (menu != null) {
					menu.close(player);
				}
				hideArmorStands(player);
				player.setGameMode(preservedGameModes.getOrDefault(player.getUniqueId(), GameMode.SURVIVAL));
			}

			editingPlayers.remove(uuid);
			editor.clearEditedRoadmap();

			if (!isEdited()) {
				stopParticleTask();
			}
		}
	}

	public void showArmorStands(Player player) {
		armorstandHandler.showNodes(roadMap.getNodes(), player);
		armorstandHandler.showEdges(roadMap.getEdges(), player);
	}

	public void hideArmorStands(Player player) {
		armorstandHandler.hideNodes(roadMap.getNodes(), player);
		armorstandHandler.hideEdges(roadMap.getEdges(), player);
	}

	public boolean isEditing(UUID uuid) {
		return editingPlayers.containsKey(uuid);
	}

	public boolean isEditing(Player player) {
		return isEditing(player.getUniqueId());
	}

	private void startParticleTask() {
		updateEditModeParticles();
	}

	private void stopParticleTask() {
		var sched = Bukkit.getScheduler();
		editModeTasks.forEach(sched::cancelTask);
	}

	/**
	 * Erstellt eine Liste aus Partikel Packets, die mithilfe eines Schedulers immerwieder an die Spieler im Editmode geschickt werden.
	 * Um gelöschte und neue Kanten darstellen zu können, muss diese Liste aus Packets aktualisiert werden.
	 * Wird asynchron ausgeführt
	 */
	public void updateEditModeParticles() {
		CompletableFuture.runAsync(() -> {

			var sched = Bukkit.getScheduler();
			editModeTasks.forEach(sched::cancelTask);

			Map<Edge, Boolean> undirected = new HashMap<>();
			for (Edge edge : roadMap.getEdges()) {
				Edge contained = roadMap.getEdge(edge.getEnd(), edge.getStart());
				if (contained != null && undirected.containsKey(contained)) {
					undirected.put(contained, true);
				} else {
					undirected.put(edge, false);
				}
			}

			Map<Color, List<Object>> packets = new HashMap<>();
			Map<Color, ParticleBuilder> particles = new HashMap<>();

			World world = roadMap.getWorld();
			for (var entry : undirected.entrySet()) {
				boolean directed = !entry.getValue();

				Vector a = entry.getKey().getStart().getPosition();
				Vector b = entry.getKey().getEnd().getPosition();
				double dist = a.distance(b);

				for (float i = 0; i < dist; i += particleDistance) {
					Color c = directed ? LerpUtils.lerp(colorFrom, colorTo, i / dist) : colorFrom;

					ParticleBuilder builder = particles.computeIfAbsent(c, k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
					packets.computeIfAbsent(c, x -> new ArrayList<>()).add(builder.setLocation(LerpUtils.lerp(a, b, i / dist).toLocation(world)).toPacket());
				}
			}
			for (var entry : packets.entrySet()) {
				editModeTasks.add(TaskManager.startSuppliedTask(entry.getValue(), tickDelay, () -> editingPlayers.keySet().stream()
						.map(Bukkit::getPlayer)
						.filter(Objects::nonNull)
						.filter(Player::isOnline)
						.collect(Collectors.toSet())));
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});
	}

	@EventHandler
	public void onNodeAssign(NodeGroupAssignEvent event) { //TODO auch bei erzeugung der heads
		editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
				event.getNodes().forEach(node -> {
					int value = node.getNodeId();
					armorstandHandler.updateNodeHead(player, value);
					armorstandHandler.renameArmorstand(player, value, Messages.formatGroup(
							player, Messages.E_NODE_NAME, ((Groupable)node).getGroups().stream()
									.map(NodeGroup::getSearchTerms)
									.flatMap(Collection::stream).collect(Collectors.toList()),
							Component::text
					));
				}));
	}

	@EventHandler
	public void onNodeCreated(NodeCreatedEvent event) {
		editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> armorstandHandler.showNode(event.getNode(), player));
	}

	@EventHandler
	public void onEdgeCreated(EdgesCreatedEvent event) {
		Collection<Edge> edges = new HashSet<>();
		for (Edge edge : event.getEdges()) {
			Edge otherDirection = roadMap.getEdge(edge.getEnd(), edge.getStart());
			if (otherDirection != null && !edges.contains(otherDirection)) {
				edges.add(otherDirection);
			}
		}
		editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
			armorstandHandler.showEdges(event.getEdges(), player);
		});
		updateEditModeParticles();
	}

	@EventHandler
	public void onNodeDeleted(NodeDeletedEvent event) {
		Collection<Edge> edges = roadMap.getEdges().stream().filter(edge -> edge.getEnd().equals(event.getNode())).collect(Collectors.toList());
		editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
			armorstandHandler.hideEdges(edges, player);
			armorstandHandler.hideNodes(Lists.newArrayList(event.getNode()), player);
		});
		updateEditModeParticles();
	}

	@EventHandler
	public void onEdgesDeleted(EdgeDeletedEvent event) {
		editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
				armorstandHandler.hideEdges(Lists.newArrayList(event.getEdge()), player));
		updateEditModeParticles();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTeleportNode(NodeTeleportEvent event) {
		Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> {
			if (event.isCancelled()) {
				return;
			}
			editingPlayers.keySet().stream()
					.map(Bukkit::getPlayer)
					.filter(Objects::nonNull)
					.forEach(player ->
							armorstandHandler.updateNodePosition(event.getNode(), player, player.getLocation(), true));
			updateEditModeParticles();
		}, 1);
	}
}
