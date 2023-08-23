package de.cubbossa.pathfinder.editmode.renderer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.editmode.clientside.ClientArmorStand;
import de.cubbossa.pathfinder.editmode.clientside.ClientEntity;
import de.cubbossa.pathfinder.editmode.clientside.PlayerSpace;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public abstract class AbstractArmorstandRenderer<T> implements GraphRenderer<Player> {


  protected final Collection<PathPlayer<Player>> players;

  final Map<UUID, PlayerSpace> playerSpaces;
  final BiMap<ClientArmorStand, T> entityNodeMap;
  final Map<Player, Set<T>> hiddenNodes;
  double renderDistance;
  double renderDistanceSquared;

  public AbstractArmorstandRenderer(JavaPlugin plugin) {
    playerSpaces = new HashMap<>();
    entityNodeMap = HashBiMap.create();
    players = new HashSet<>();
    hiddenNodes = new ConcurrentHashMap<>();

    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
      for (PathPlayer<Player> player : players) {
        Collection<CompletableFuture<?>> futures = new HashSet<>();
        HashSet<T> show = new HashSet<>();
        HashSet<T> hide = new HashSet<>();
        for (T node : entityNodeMap.values()) {
          futures.add(retrieveFrom(node).thenAccept(location -> {
            if (BukkitVectorUtils.toInternal(location).distanceSquared(player.getLocation()) >= renderDistanceSquared) {
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
          col.addAll(hide);
          col.removeAll(show);
          showElements(show, player.unwrap());
          hideElements(hide, player.unwrap());
        }).exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
      }
    }, 1, 5);
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

  public void showElements(Collection<T> elements, Player player) {
    for (T element : elements) {
      showElement(element, player);
    }
  }

  public void showElement(T element, Player player) {
    if (entityNodeMap.values().stream().anyMatch(e -> equals(element, e))) {
      updateElement(element, player);
      return;
    }
    retrieveFrom(element).thenAccept(location -> {
      if (location.distanceSquared(player.getLocation()) > renderDistanceSquared) {
        hiddenNodes.computeIfAbsent(player, player1 -> new HashSet<>()).add(element);
        return;
      }
      ArmorStand armorStand = ps(player).spawnEntity(ArmorStand.class, location);
      armorStand.setSmall(isSmall(element));
      ((ClientEntity) armorStand).setCustomName(getName(element));
      armorStand.setBasePlate(false);
      armorStand.setVisible(false);
      armorStand.getEquipment().setHelmet(head(element));
      ps(player).announce(armorStand);

      entityNodeMap.put((ClientArmorStand) armorStand, element);
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  private PlayerSpace ps(Player player) {
    return playerSpaces.computeIfAbsent(player.getUniqueId(), PlayerSpace::create);
  }

  public void updateElement(T element, Player player) {
    T prev = entityNodeMap.values().stream().filter(e -> equals(element, e)).findAny().orElseThrow();
    retrieveFrom(prev).thenAccept(prevLoc -> {
      retrieveFrom(element).thenAccept(loc -> {
        // update position if position changed
        if (!prevLoc.equals(loc)) {
          Entity entity = entityNodeMap.inverse().get(element);
          entity.teleport(loc);
          ps(player).announce(entity);
        }
      });
    });
  }

  public void hideElements(Collection<T> elements, Player player) {
    Map<T, ClientArmorStand> nodeEntityMap = entityNodeMap.inverse();
    new HashSet<>(elements).forEach(e -> {
      Entity present = nodeEntityMap.remove(e);
      if (present != null) {
        present.remove();
      }
    });
    ps(player).announceEntityRemovals();
  }
}