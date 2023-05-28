package de.cubbossa.pathfinder.editmode.renderer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.IntPair;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public abstract class AbstractArmorstandRenderer<T> implements GraphRenderer<Player> {

  private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

  private static final int META_INDEX_FLAGS = 0;
  private static final int META_INDEX_NAME = 2;
  private static final int META_INDEX_NAME_VISIBLE = 3;
  private static final int META_INDEX_CHILD = 15;
  private static final int META_INDEX_HEAD_EULER = 16;
  private static final int META_INDEX_NO_GRAVITY = 5;
  private static final byte META_FLAG_INVISIBLE = 0x20;
  private static final byte META_FLAG_SMALL = 0x01;

  static int entityId = 10_000;
  protected final Collection<PathPlayer<Player>> players;
  final ProtocolManager protocolManager;
  final Map<T, Integer> nodeEntityMap;
  final Map<Integer, T> entityNodeMap;
  final Map<Player, Set<T>> hiddenNodes;
  double renderDistance;
  double renderDistanceSquared;

  public AbstractArmorstandRenderer(JavaPlugin plugin) {
    entityId = 0xffffabcd;
    protocolManager = ProtocolLibrary.getProtocolManager();

    nodeEntityMap = new HashMap<>();
    entityNodeMap = new HashMap<>();
    players = new HashSet<>();
    hiddenNodes = new HashMap<>();

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (PathPlayer<Player> player : players) {
        Collection<CompletableFuture<?>> futures = new HashSet<>();
        HashSet<T> show = new HashSet<>();
        HashSet<T> hide = new HashSet<>();
        for (T node : nodeEntityMap.keySet()) {
          futures.add(retrieveFrom(node).thenAccept(location -> {
            if (BukkitVectorUtils.toInternal(location).distanceSquared(player.getLocation()) > renderDistanceSquared) {
              hide.add(node);
            }
          }));
        }
        for (T node : hiddenNodes.getOrDefault(player.unwrap(), new HashSet<>())) {
          futures.add(retrieveFrom(node).thenAccept(location -> {
            if (BukkitVectorUtils.toInternal(location).distanceSquared(player.getLocation()) < renderDistanceSquared) {
              show.add(node);
            }
          }));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
          Collection<T> col = hiddenNodes.computeIfAbsent(player.unwrap(), u -> new HashSet<>());
          col.addAll(show);
          col.removeAll(hide);
          showElements(show, player.unwrap());
          hideElements(hide, player.unwrap());
        });
      }
    }, 1, 5);

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
          T element = entityNodeMap.get(entityId);
          if (element == null) {
            throw new IllegalStateException("ClientNodeHandler Tables off sync!");
          }
          Player player = event.getPlayer();
          int slot = player.getInventory().getHeldItemSlot();
          Menu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
          if (menu == null) {
            return;
          }
          event.setCancelled(true);
          Action<TargetContext<T>> action = handleInteract(player, slot, left);
          menu.handleInteract(action,
              new TargetContext<>(player, menu, slot, action, true, element));
        }
      }
    });
  }

  public void setRenderDistance(double renderDistance) {
    this.renderDistance = renderDistance;
    this.renderDistanceSquared = Math.pow(renderDistance, 2);
  }

  abstract boolean equals(T a, T b);

  abstract ItemStack head(T element);

  abstract CompletableFuture<Location> retrieveFrom(T element);

  abstract Action<TargetContext<T>> handleInteract(Player player, int slot, boolean left);

  abstract boolean isSmall(T element);

  abstract @Nullable Component getName(T element);

  private Optional<T> element(int id) {
    return Optional.ofNullable(entityNodeMap.get(id));
  }

  private Optional<Integer> id(T element) {
    return nodeEntityMap.entrySet().stream().filter(t -> equals(t.getKey(), element)).findAny().map(Map.Entry::getValue);
  }

  public void showElements(Collection<T> elements, Player player) {
    for (T element : elements) {
      showElement(element, player);
    }
  }

  public void showElement(T element, Player player) {
    if (nodeEntityMap.keySet().stream().anyMatch(e -> equals(element, e))) {
      updateElement(element, player);
      return;
    }
    retrieveFrom(element).thenAccept(location -> {
      int id = spawnArmorstand(player, location, getName(element), isSmall(element));

      nodeEntityMap.put(element, id);
      entityNodeMap.put(id, element);

      IntPair key = new IntPair(location.getChunk().getX(), location.getChunk().getZ());

      equipArmorstand(player, id, new ItemStack[]{null, null, null, null, null, head(element)});
    });
  }

  public void updateElement(T element, Player player) {
    T prev = nodeEntityMap.keySet().stream().filter(e -> equals(element, e)).findAny().orElseThrow();
    retrieveFrom(prev).thenAccept(prevLoc -> {
      retrieveFrom(element).thenAccept(loc -> {
        // update position if position changed
        if (!prevLoc.equals(loc)) {
          teleportArmorstand(player, id(element).orElseThrow(), loc);
        }
      });
    });
  }

  public void hideElements(Collection<T> elements, Player player) {
    removeArmorstand(player,
        elements.stream().map(nodeEntityMap::get).filter(Objects::nonNull).toList());

    elements.forEach(nodeEntityMap::remove);
    new HashMap<>(entityNodeMap).entrySet().stream().filter(e -> elements.contains(e.getValue()))
        .map(Map.Entry::getKey).forEach(entityNodeMap::remove);
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

    int entityId = AbstractArmorstandRenderer.entityId++;

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

  public void updateNodeHead(Player player, T element) {
    Integer id = nodeEntityMap.get(element);
    if (id == null) {
      throw new RuntimeException(
          "Trying to update armorstand that was not registered for client side display.");
    }
    equipArmorstand(player, id, new ItemStack[]{null, null, null, null, null, head(element)});
  }

  public void renameArmorstand(Player player, T element, @Nullable Component name) {
    Integer id = nodeEntityMap.get(element);
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

  public void setHeadRotation(Player player, int id, Vector direction) {
    Location location = new Location(null, 0, 0, 0);
    location.setDirection(direction);
    Vector3F v = new Vector3F(
        location.getPitch(),
        location.getYaw(),
        0
    );

    WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
    dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(META_INDEX_HEAD_EULER,
        WrappedDataWatcher.Registry.getVectorSerializer()), v);
    sendMeta(player, id, dataWatcher);
  }
}