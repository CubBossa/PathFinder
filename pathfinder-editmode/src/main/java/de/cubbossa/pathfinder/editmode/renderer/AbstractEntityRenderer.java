package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.editmode.utils.SharedEntityPool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public abstract class AbstractEntityRenderer<ElementT, DisplayT extends Display> implements GraphRenderer<Player> {

  protected final Collection<PathPlayer<Player>> players;
  final SharedEntityPool<ElementT, DisplayT> entityPool;
  final Map<ElementT, DisplayT> nodeEntityMap;
  final Map<DisplayT, ElementT> entityNodeMap;
  final Map<Player, Set<ElementT>> hiddenNodes;
  double renderDistance;
  double renderDistanceSquared;

  public AbstractEntityRenderer(JavaPlugin plugin, Class<DisplayT> displayClass) {

    entityPool = new SharedEntityPool<>(plugin, 32, displayClass);
    nodeEntityMap = new ConcurrentHashMap<>();
    entityNodeMap = new ConcurrentHashMap<>();
    hiddenNodes = new ConcurrentHashMap<>();
    players = new HashSet<>();

//    protocolManager.addPacketListener(new PacketAdapter(plugin,
//        ListenerPriority.NORMAL,
//        PacketType.Play.Client.USE_ENTITY) {
//
//      @Override
//      public void onPacketReceiving(PacketEvent event) {
//        PacketContainer packet = event.getPacket();
//        int entityId = packet.getIntegers().read(0);
//
//        boolean left = packet.getEnumEntityUseActions().read(0).getAction()
//            .equals(EnumWrappers.EntityUseAction.ATTACK);
//        if (entityNodeMap.containsKey(entityId)) {
//          ElementT element = entityNodeMap.get(entityId);
//          if (element == null) {
//            throw new IllegalStateException("ClientNodeHandler Tables off sync!");
//          }
//          Player player = event.getPlayer();
//          int slot = player.getInventory().getHeldItemSlot();
//          Menu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
//          if (menu == null) {
//            return;
//          }
//          event.setCancelled(true);
//          Action<TargetContext<ElementT>> action = handleInteract(player, slot, left);
//          menu.handleInteract(action,
//              new TargetContext<>(player, menu, slot, action, true, element));
//        }
//      }
//    });
  }

  public void setRenderDistance(double renderDistance) {
    this.renderDistance = renderDistance;
    this.renderDistanceSquared = Math.pow(renderDistance, 2);
  }

  abstract boolean equals(ElementT a, ElementT b);

  abstract CompletableFuture<Location> location(ElementT element);

  abstract void render(ElementT element, DisplayT entity);

//  abstract Action<TargetContext<ElementT>> handleInteract(Player player, int slot, boolean left);

  public void showElements(Collection<ElementT> elements, Player player) {
    for (ElementT element : elements) {
      showElement(element, player);
    }
  }

  public void showElement(ElementT element, Player player) {
    if (nodeEntityMap.keySet().stream().anyMatch(e -> equals(element, e))) {
      updateElement(element);
      return;
    }

    location(element).thenAcceptAsync(location -> {

      DisplayT entity = entityPool.get(location, element, player);
      entity.setViewRange((float) (renderDistance / 64.));

      nodeEntityMap.put(element, entity);
      entityNodeMap.put(entity, element);

      render(element, entity);
    }, BukkitPathFinder.mainThreadExecutor()).exceptionally(throwable -> {
      throwable.printStackTrace();
      hideElements(Collections.singleton(element), player);
      return null;
    });
  }

  public void updateElement(ElementT element) {
    DisplayT display = nodeEntityMap.get(element);
    Location prev = display.getLocation();
    location(element).thenAccept(loc -> {
      // update position if position changed
      if (!prev.equals(loc)) {
        display.teleport(loc);
      }
    });
  }

  public void hideElements(Collection<ElementT> elements, Player player) {
    elements.forEach(e -> {
      nodeEntityMap.remove(e);
      entityPool.destroy(e, player);
    });
    elements.stream().map(nodeEntityMap::get).filter(Objects::nonNull).forEach(entityNodeMap::remove);
  }

  @Override
  public void close() throws Exception {
    entityPool.close();
  }
}