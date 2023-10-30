package de.cubbossa.pathfinder.editmode.renderer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import de.cubbossa.cliententities.PlayerSpace;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public abstract class AbstractEntityRenderer<ElementT, DisplayT extends Display> implements GraphRenderer<Player> {

  final PlayerSpace playerSpace;
  protected final Collection<PathPlayer<Player>> players;
  final Class<DisplayT> entityClass;
  final BiMap<DisplayT, ElementT> entityNodeMap;
  final BiMap<Interaction, ElementT> interactionNodeMap;
  double renderDistance;
  double renderDistanceSquared;

  private Collection<PlayerSpace.ListenerHandle> listeners = new HashSet<>();

  public AbstractEntityRenderer(JavaPlugin plugin, Class<DisplayT> displayClass) {
    this.entityClass = displayClass;
    this.playerSpace = PlayerSpace.builder().withEventSupport().build();

    entityNodeMap = Maps.synchronizedBiMap(HashBiMap.create());
    interactionNodeMap = Maps.synchronizedBiMap(HashBiMap.create());
    players = new HashSet<>();

    listeners.add(playerSpace.registerListener(PlayerInteractAtEntityEvent.class, this::onClick));
    listeners.add(playerSpace.registerListener(EntityDamageByEntityEvent.class, this::onHit));
  }

  @Override
  public void close() throws Exception {
    listeners.forEach(playerSpace::unregisterListener);
    playerSpace.close();
  }

  public void setRenderDistance(double renderDistance) {
    this.renderDistance = renderDistance;
    this.renderDistanceSquared = Math.pow(renderDistance, 2);
  }

  abstract boolean equals(ElementT a, ElementT b);

  abstract CompletableFuture<Location> location(ElementT element);

  abstract void render(ElementT element, DisplayT entity);

  abstract void hitbox(ElementT element, Interaction entity);

  abstract Action<TargetContext<ElementT>> handleInteract(Player player, int slot, boolean left);

  public CompletableFuture<Void> showElements(Collection<ElementT> elements, Player player) {
    return CompletableFuture.allOf(elements.stream()
            .map(e -> showElement(e, player))
            .toArray(CompletableFuture[]::new))
        .thenRun(playerSpace::announce);
  }

  public CompletableFuture<Void> showElement(ElementT element, Player player) {
    playerSpace.addPlayerIfAbsent(player);
    if (entityNodeMap.inverse().containsKey(element)) {
      return updateElement(element, player);
    }

    return location(element).thenAccept(location -> {

      DisplayT entity = (DisplayT) playerSpace.spawn(location, entityClass);

      entity.setViewRange((float) (renderDistance / 64.));

      entityNodeMap.inverse().put(element, entity);
      entityNodeMap.put(entity, element);

      render(element, entity);

      Interaction interaction = playerSpace.spawn(location, Interaction.class);
      interactionNodeMap.put(interaction, element);

      hitbox(element, interaction);

    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      hideElements(Collections.singleton(element), player);
      return null;
    });
  }

  public CompletableFuture<Void> updateElement(ElementT element, Player player) {
    DisplayT display = entityNodeMap.inverse().get(element);
    if (display.isDead()) {
      entityNodeMap.remove(display);
      return showElement(element, player);
    }
    Location prev = display.getLocation();
    return location(element).thenAccept(loc -> {
      // update position if position changed
      if (!prev.equals(loc)) {
        display.teleport(loc);

        Interaction interaction = interactionNodeMap.inverse().get(element);
        interaction.teleport(loc);
        hitbox(element, interaction);
      }
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  public void hideElements(Collection<ElementT> elements, Player player) {
    Map<ElementT, DisplayT> map = entityNodeMap.inverse();
    Map<ElementT, Interaction> interact = interactionNodeMap.inverse();

    // make hashset to make sure that elements was not the keyset of either map
    for (ElementT e : new HashSet<>(elements)) {
      Entity e1 = map.remove(e);
      Entity e2 = interact.remove(e);
      if (e1 == null && e2 == null) {
        continue;
      }
      if (e1 != null) {
        e1.remove();
      }
      if (e2 != null) {
        e2.remove();
      }
    }
    playerSpace.announce();
  }

  public void onClick(PlayerInteractEntityEvent e) {
    if (e.getRightClicked() instanceof Interaction interaction) {
      handleInteract(interaction, e.getPlayer(), false);
    }
  }

  public void onHit(EntityDamageByEntityEvent e) {
    if (e.getEntity() instanceof Interaction interaction && e.getDamager() instanceof Player player) {
      handleInteract(interaction, player, true);
    }
  }

  private void handleInteract(Interaction e, Player player, boolean left) {
    ElementT element = interactionNodeMap.get(e);
    if (element == null) {
      return;
    }
    int slot = player.getInventory().getHeldItemSlot();
    Menu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
    if (menu == null) {
      return;
    }
    Action<TargetContext<ElementT>> action = handleInteract(player, slot, left);
    menu.handleInteract(action, new TargetContext<>(player, menu, slot, action, true, element));
  }
}