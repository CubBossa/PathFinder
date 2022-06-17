package de.bossascrew.pathfinder.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import de.bossascrew.pathfinder.node.Edge;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
@Setter
public class FakeArmorstandHandler {

	private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

	private static final Vector ARMORSTAND_OFFSET = new Vector(0, -1.75, 0);
	private static final Vector ARMORSTAND_CHILD_OFFSET = new Vector(0, -1, 0);

	private static final int META_INDEX_FLAGS = 0;
	private static final int META_INDEX_NAME = 2;
	private static final int META_INDEX_NAME_VISIBLE = 3;
	private static final int META_INDEX_CHILD = 15;
	private static final int META_INDEX_NO_GRAVITY = 5;
	private static final byte META_FLAG_INVISIBLE = 0x20;
	private static final byte META_FLAG_SMALL = 0x01;

	private static int entityId = 10_000;
	private final ProtocolManager protocolManager;

	private ItemStack nodeHead;
	private ItemStack edgeHead;

	private final Map<Pair<Integer, Integer>, Collection<Node>> chunkNodeMap;
	private final Map<Pair<Integer, Integer>, Collection<Edge>> chunkEdgeMap;
	private final Map<Node, Integer> nodeEntityMap;
	private final Map<Edge, Integer> edgeEntityMap;

	public FakeArmorstandHandler(JavaPlugin plugin) {
		protocolManager = ProtocolLibrary.getProtocolManager();

		chunkNodeMap = new HashMap<>();
		chunkEdgeMap = new HashMap<>();
		nodeEntityMap = new HashMap<>();
		edgeEntityMap = new HashMap<>();

		protocolManager.addPacketListener(new PacketAdapter(plugin,
				ListenerPriority.NORMAL,
				PacketType.Play.Server.MAP_CHUNK) {

			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				int chunkX = packet.getIntegers().read(0);
				int chunkY = packet.getIntegers().read(1);

				var key = new Pair<>(chunkX, chunkY);
				Collection<Node> nodes = chunkNodeMap.get(key);
				if (nodes != null) {
					showNodes(nodes, event.getPlayer());
				}
				Collection<Edge> edges = chunkEdgeMap.get(key);
				if (edges != null) {
					showEdges(edges, event.getPlayer());
				}
			}
		});
	}

	public void showNodes(Collection<Node> nodes, Player player) {
		for (Node node : nodes) {
			showNode(node, player);
		}
	}

	public void showNode(Node node, Player player) {
		Location location = node.getLocation();
		int id = spawnArmorstand(player, location);
		equipArmorstand(player, id, new ItemStack[]{null, null, null, null, null, null, nodeHead});
		setupMeta(player, id, GSON.serialize(node.getDisplayName()), false);
		nodeEntityMap.put(node, id);
		var key = new Pair<>(location.getChunk().getX(), location.getChunk().getZ());
		Collection<Node> inner = chunkNodeMap.getOrDefault(key, new HashSet<>());
		inner.add(node);
		chunkNodeMap.put(key, inner);
	}

	public void showEdges(Collection<Edge> edges, Player player) {
		Map<Edge, Boolean> undirected = new HashMap<>();
		for (Edge edge : edges) {
			Edge contained = undirected.keySet().stream().filter(e -> e.getStart().equals(edge.getEnd()) && e.getEnd().equals(edge.getStart())).findFirst().orElse(null);
			if (contained != null) {
				undirected.put(contained, true);
			} else {
				undirected.put(edge, false);
			}
		}
		for (var entry : undirected.entrySet()) {
			showEdge(entry.getKey(), player, entry.getValue());
		}
	}

	public void showEdge(Edge edge, Player player, boolean undirected) {
		Vector pos = edge.getStart().getPosition().clone().add(
				edge.getEnd().getPosition().subtract(edge.getStart().getPosition()).multiply(.5f));
		Location location = pos.toLocation(RoadMapHandler.getInstance().getRoadMap(edge.getStart().getRoadMapKey()).getWorld());
		int id = spawnArmorstand(player, location);
		equipArmorstand(player, id, new ItemStack[]{null, null, null, null, null, null, edgeHead});
		setupMeta(player, id, GSON.serialize(edge.getStart().getDisplayName()
								.append(Component.text(undirected ? " <-> " : " -> "))
								.append(edge.getEnd().getDisplayName())), true);
		edgeEntityMap.put(edge, id);
		var key = new Pair<>(location.getChunk().getX(), location.getChunk().getZ());
		Collection<Edge> inner = chunkEdgeMap.getOrDefault(key, new HashSet<>());
		inner.add(edge);
		chunkEdgeMap.put(key, inner);
	}

	public void hideNodes(Collection<Node> nodes, Player player) {
		removeArmorstand(player, nodes.stream().map(nodeEntityMap::get).filter(Objects::nonNull).toList());
		nodes.forEach(nodeEntityMap::remove);
		nodes.forEach(node -> chunkNodeMap.remove(new Pair<>((int) node.getPosition().getX() / 16, (int) node.getPosition().getZ() / 16)));
	}

	public void hideEdges(Collection<Edge> edges, Player player) {
		removeArmorstand(player, edges.stream().map(edgeEntityMap::get).filter(Objects::nonNull).toList());
		edges.forEach(edgeEntityMap::remove);
		edges.forEach(edge -> {
			Vector pos = edge.getStart().getPosition().clone().add(
					edge.getEnd().getPosition().subtract(edge.getStart().getPosition()).multiply(.5f));
			chunkNodeMap.remove(new Pair<>((int) pos.getX() / 16, (int) pos.getZ() / 16));
		});
	}

	public void updateNodePosition(Node node, boolean updateEdges) {

	}

	public void updateNodeName(Node node, boolean updateEdges) {

	}

	private void sendMeta(Player player, int id, WrappedDataWatcher watcher) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, id);
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public int spawnArmorstand(Player player, Location location) {

		int entityId = FakeArmorstandHandler.entityId++;

		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
		packet.getModifier().writeDefaults();
		packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
		packet.getIntegers().write(0, entityId);
		packet.getUUIDs().write(0, UUID.randomUUID());
		packet.getDoubles().write(0, location.getX());
		packet.getDoubles().write(1, location.getY());
		packet.getDoubles().write(2, location.getZ());
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		setupMeta(player, entityId, "Hugobert", true);
		return entityId;
	}

	private void setupMeta(Player player, int id, String nameJson, boolean small) {
		WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
		dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME, WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
				Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Component.text(nameJson))).getHandle()));
		dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME_VISIBLE, WrappedDataWatcher.Registry.get(Boolean.class)), true);
		dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_FLAGS, WrappedDataWatcher.Registry.get(Byte.class)), META_FLAG_INVISIBLE);
		if (small) {
			dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_CHILD, WrappedDataWatcher.Registry.get(Byte.class)), META_FLAG_SMALL);
		}
		dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NO_GRAVITY, WrappedDataWatcher.Registry.get(Boolean.class)), true);
		sendMeta(player, id, dataWatcher);
	}

	public void removeArmorstand(Player player, List<Integer> ids) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getModifier().writeDefaults();
		packet.getIntLists().write(0, ids);
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void teleportArmorstand(Player player, Integer id, Location location) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
		packet.getModifier().writeDefaults();
		packet.getIntegers().write(0, id);
		packet.getDoubles().write(0, location.getX());
		packet.getDoubles().write(1, location.getY());
		packet.getDoubles().write(2, location.getZ());
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void equipArmorstand(Player player, Integer id, ItemStack[] equip) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

		List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
		for (EnumWrappers.ItemSlot slot : EnumWrappers.ItemSlot.values()) {
			pairList.add(new Pair<>(slot, equip[slot.ordinal()]));
		}

		packet.getIntegers().write(0, id);
		packet.getSlotStackPairLists().write(0, pairList);

		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void renameArmorstand(Player player, Integer id, String nameJson) {
		WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
		dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME, WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
				Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Component.text(nameJson))).getHandle()));
		dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME_VISIBLE, WrappedDataWatcher.Registry.get(Boolean.class)), true);
		sendMeta(player, id, dataWatcher);
	}
}