package de.cubbossa.pathfinder.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ClientNodeHandler {

  public static final Action<TargetContext<Node>> RIGHT_CLICK_NODE = new Action<>();
  public static final Action<TargetContext<Node>> LEFT_CLICK_NODE = new Action<>();
  public static final Action<TargetContext<Edge>> RIGHT_CLICK_EDGE = new Action<>();
  public static final Action<TargetContext<Edge>> LEFT_CLICK_EDGE = new Action<>();

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
  private final Map<IntPair, Collection<Node>> chunkNodeMap;
  private final Map<IntPair, Collection<Edge>> chunkEdgeMap;
  private final Map<Node, Integer> nodeEntityMap;
  private final Map<Integer, Node> entityNodeMap;
  private final Map<Edge, Integer> edgeEntityMap;
  private final Map<Integer, Edge> entityEdgeMap;
  private ItemStack nodeSingleHead = ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_GREEN);
  private ItemStack nodeGroupHead = ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_BLUE);
  private ItemStack edgeHead = ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_ORANGE);

  public ClientNodeHandler(JavaPlugin plugin) {
    entityId = Bukkit.getWorlds().stream()
        .mapToInt(w -> w.getEntities().stream().mapToInt(Entity::getEntityId).max().orElse(0)).max()
        .orElse(0) + 10_000;
    protocolManager = ProtocolLibrary.getProtocolManager();

    chunkNodeMap = new HashMap<>();
    chunkEdgeMap = new HashMap<>();
    nodeEntityMap = new HashMap<>();
    entityNodeMap = new TreeMap<>();
    edgeEntityMap = new HashMap<>();
    entityEdgeMap = new TreeMap<>();

    protocolManager.addPacketListener(new PacketAdapter(plugin,
        ListenerPriority.NORMAL,
        PacketType.Play.Server.MAP_CHUNK) {

      @Override
      public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        int chunkX = packet.getIntegers().read(0);
        int chunkY = packet.getIntegers().read(1);

        var key = new IntPair(chunkX, chunkY);
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

    protocolManager.addPacketListener(new PacketAdapter(plugin,
        ListenerPriority.NORMAL,
        PacketType.Play.Client.USE_ENTITY) {

      @Override
      public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        int entityId = packet.getIntegers().read(0);

        boolean left = packet.getEnumEntityUseActions().read(0).getAction()
            .equals(EnumWrappers.EntityUseAction.ATTACK);
        if (entityNodeMap.containsKey(entityId)) {
          Node node = entityNodeMap.get(entityId);
          if (node == null) {
            throw new IllegalStateException("ClientNodeHandler Tables off sync!");
          }
          Player player = event.getPlayer();
          int slot = player.getInventory().getHeldItemSlot();
          Menu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
          if (menu == null) {
            return;
          }
          event.setCancelled(true);
          Action<TargetContext<Node>> action = left ? LEFT_CLICK_NODE : RIGHT_CLICK_NODE;
          menu.handleInteract(action, new TargetContext<>(player, menu, slot, action, true, node));
        }
        if (entityEdgeMap.containsKey(entityId)) {
          Edge edge = entityEdgeMap.get(entityId);
          if (edge == null) {
            throw new IllegalStateException("ClientNodeHandler Tables off sync!");
          }
          event.setCancelled(true);
          Player player = event.getPlayer();
          int slot = player.getInventory().getHeldItemSlot();
          Menu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
          Action<TargetContext<Edge>> action = left ? LEFT_CLICK_EDGE : RIGHT_CLICK_EDGE;
          menu.handleInteract(action, new TargetContext<>(player, menu, slot, action, true, edge));
        }
      }
    });
  }

  public static IntPair locationToChunkIntPair(Location location) {
    return new IntPair(location.getChunk().getX(), location.getChunk().getZ());
  }

  public void showNodes(Collection<Node> nodes, Player player) {
    for (Node node : nodes) {
      showNode(node, player);
    }
  }

  public void showNode(Node node, Player player) {
    Location location = node.getLocation();
    int id = spawnArmorstand(player, location, null, false);

    nodeEntityMap.put(node, id);
    entityNodeMap.put(id, node);

    IntPair key = new IntPair(location.getChunk().getX(), location.getChunk().getZ());
    chunkNodeMap.computeIfAbsent(key, intPair -> new HashSet<>()).add(node);

    equipArmorstand(player, id, new ItemStack[] {null, null, null, null, null,
        node instanceof Groupable groupable && groupable.getGroups().size() >= 1 ? nodeGroupHead :
            nodeSingleHead});
    updateNodeName(player, node);
  }

  public void updateNodeName(Player player, Node node) {
    renameArmorstand(player, node,
        node instanceof Groupable groupable && !groupable.getGroups().isEmpty() ?
            Messages.formatGroupConcat(
                player, Messages.E_NODE_NAME, ((Groupable) node).getGroups().stream()
                    .map(NodeGroup::getSearchTerms)
                    .flatMap(Collection::stream).collect(Collectors.toList()),
                t -> Component.text(t.getIdentifier())
            ) : null);
  }

  public void showEdges(Collection<Edge> edges, Player player) {
    Map<Edge, Edge> undirected = new HashMap<>();
    for (Edge edge : edges) {
      Edge contained = undirected.keySet().stream()
          .filter(e -> e.getStart().equals(edge.getEnd()) && e.getEnd().equals(edge.getStart()))
          .findFirst().orElse(null);
      if (contained != null) {
        undirected.put(contained, edge);
      } else {
        undirected.put(edge, null);
      }
    }
    for (var entry : undirected.entrySet()) {
      showEdge(entry.getKey(), entry.getValue(), player);
    }
  }

  public void showEdge(Edge edge, @Nullable Edge otherDirection, Player player) {
    Location location =
        LerpUtils.lerp(edge.getStart().getLocation(), edge.getEnd().getLocation(), .3f);
    int id = spawnArmorstand(player, location, null, true);
    equipArmorstand(player, id, new ItemStack[] {null, null, null, null, null, edgeHead});

    edgeEntityMap.put(edge, id);
    entityEdgeMap.put(id, edge);

    IntPair key = locationToChunkIntPair(location);
    chunkEdgeMap.computeIfAbsent(key, intPair -> new HashSet<>()).add(edge);

    if (otherDirection != null) {
      showEdge(otherDirection, null, player);
    }
  }

  public void hideNodes(Collection<Node> nodes, Player player) {
    removeArmorstand(player,
        nodes.stream().map(nodeEntityMap::get).filter(Objects::nonNull).toList());

    nodes.forEach(nodeEntityMap::remove);
    new HashMap<>(entityNodeMap).entrySet().stream().filter(e -> nodes.contains(e.getValue()))
        .map(Map.Entry::getKey).forEach(entityNodeMap::remove);
    nodes.forEach(node -> chunkNodeMap.remove(locationToChunkIntPair(node.getLocation())));
  }

  public void hideEdges(Collection<Edge> edges, Player player) {
    removeArmorstand(player,
        edges.stream().map(edgeEntityMap::get).filter(Objects::nonNull).toList());
    edges.forEach(edgeEntityMap::remove);
    edges.forEach(edge -> {
      Location pos = edge.getStart().getLocation().clone().add(
          edge.getEnd().getLocation().clone().subtract(edge.getStart().getLocation())
              .multiply(.5f));
      chunkEdgeMap.remove(locationToChunkIntPair(pos));
    });
  }

  public void updateNodePosition(Node node, Player player, Location location, boolean updateEdges) {
    teleportArmorstand(player, nodeEntityMap.get(node), location.clone().add(ARMORSTAND_OFFSET));
    if (updateEdges) {
      RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(node.getRoadMapKey());
      if (roadMap == null) {
        throw new RuntimeException("Roadmap unexpectedly null.");
      }
      roadMap.getEdges().stream()
          .filter(edge -> edge.getStart().equals(node) || edge.getEnd().equals(node))
          .forEach(e -> {
            teleportArmorstand(player, edgeEntityMap.get(e),
                LerpUtils.lerp(e.getStart().getLocation(), e.getEnd().getLocation(), 0.3f)
                    .clone()
                    .add(ARMORSTAND_CHILD_OFFSET));
          });
    }
  }

  private void sendMeta(Player player, int id, WrappedDataWatcher watcher) {
    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
    packet.getIntegers().write(0, id);

    if (MinecraftVersion.getCurrentVersion().isAtLeast(new MinecraftVersion("1.19.3"))) {
      final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
      watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
        final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject =
            entry.getWatcherObject();
        wrappedDataValueList.add(
            new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(),
                entry.getRawValue()));
      });
      packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
    } else {
      packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
    }
    protocolManager.sendServerPacket(player, packet);
  }

  public synchronized int spawnArmorstand(Player player, Location location,
                                          @Nullable Component name, boolean small) {

    location = location.clone().add((small ? ARMORSTAND_CHILD_OFFSET : ARMORSTAND_OFFSET));
    int entityId = ClientNodeHandler.entityId++;

    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
    packet.getModifier().writeDefaults();
    packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
    packet.getIntegers().write(0, entityId);
    packet.getUUIDs().write(0, UUID.randomUUID());
    packet.getDoubles().write(0, location.getX());
    packet.getDoubles().write(1, location.getY());
    packet.getDoubles().write(2, location.getZ());
    protocolManager.sendServerPacket(player, packet);
    setupMeta(player, entityId, name, small);
    return entityId;
  }

  private void setupMeta(Player player, int id, @Nullable Component name, boolean small) {
    WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

    if (name != null) {
      dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME,
              WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
          Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(name))
              .getHandle()));
      dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME_VISIBLE,
          WrappedDataWatcher.Registry.get(Boolean.class)), true);
    }

    dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_FLAGS,
        WrappedDataWatcher.Registry.get(Byte.class)), META_FLAG_INVISIBLE);
    if (small) {
      dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_CHILD,
          WrappedDataWatcher.Registry.get(Byte.class)), META_FLAG_SMALL);
    }
    dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NO_GRAVITY,
        WrappedDataWatcher.Registry.get(Boolean.class)), true);
    sendMeta(player, id, dataWatcher);
  }

  public void removeArmorstand(Player player, List<Integer> ids) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
    packet.getModifier().writeDefaults();
    packet.getIntLists().write(0, ids);
    protocolManager.sendServerPacket(player, packet);
  }

  public void teleportArmorstand(Player player, Integer id, Location location) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
    packet.getModifier().writeDefaults();
    packet.getIntegers().write(0, id);
    packet.getDoubles().write(0, location.getX());
    packet.getDoubles().write(1, location.getY());
    packet.getDoubles().write(2, location.getZ());
    protocolManager.sendServerPacket(player, packet);
  }

  public void equipArmorstand(Player player, Integer id, ItemStack[] equip) {
    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

    List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
    for (EnumWrappers.ItemSlot slot : EnumWrappers.ItemSlot.values()) {
      pairList.add(new Pair<>(slot, equip[slot.ordinal()]));
    }

    packet.getIntegers().write(0, id);
    packet.getSlotStackPairLists().write(0, pairList);

    protocolManager.sendServerPacket(player, packet);
  }

  public void updateNodeHead(Player player, Node node) {
    Integer id = nodeEntityMap.get(node);
    if (id == null) {
      throw new RuntimeException(
          "Trying to update armorstand that was not registered for client side display.");
    }
    equipArmorstand(player, id, new ItemStack[] {null, null, null, null, null,
        node instanceof Groupable groupable && groupable.getGroups().size() >= 1 ? nodeGroupHead :
            nodeSingleHead});
  }

  public void renameArmorstand(Player player, Node node, @Nullable Component name) {
    Integer id = nodeEntityMap.get(node);
    if (id == null) {
      throw new RuntimeException(
          "Trying to update armorstand that was not registered for client side display.");
    }

    WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
    if (name == null) {
      dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME_VISIBLE,
          WrappedDataWatcher.Registry.get(Boolean.class)), false);
      sendMeta(player, id, dataWatcher);
      return;
    }
    dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME,
            WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
        Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(name))
            .getHandle()));
    dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_NAME_VISIBLE,
        WrappedDataWatcher.Registry.get(Boolean.class)), true);
    sendMeta(player, id, dataWatcher);
  }
}