package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.discovery.AbstractDiscoveryModule;
import de.cubbossa.pathfinder.group.DiscoverProgressModifier;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.StorageAdapter;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Getter
public class DiscoverProgressModifierImpl implements DiscoverProgressModifier {

  private final NamespacedKey owningGroup;
  private final String nameFormat;
  private final Component displayName;

  public DiscoverProgressModifierImpl(NamespacedKey ownerGroup, String nameFormat) {
    this.owningGroup = ownerGroup;
    this.nameFormat = nameFormat;
    this.displayName = PathFinder.get().getMiniMessage().deserialize(nameFormat);
  }

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }

  @Override
  public CompletableFuture<Double> calculateProgress(UUID playerId) {
    StorageAdapter storage = PathFinder.get().getStorage();
    AbstractDiscoveryModule<Player> dh = AbstractDiscoveryModule.getInstance();

    return storage.loadGroup(owningGroup).thenCompose(group -> {
      return storage.loadGroups(group.orElseThrow()).thenApply(m -> {
        Collection<NodeGroup> discoverableGroupsWithin = m.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .filter(g -> g.hasModifier(DiscoverableModifier.KEY))
                .toList();
        int all = discoverableGroupsWithin.size();
        if (all == 0) {
          return 0.;
        }
        int discovered = (int) discoverableGroupsWithin.stream()
                .filter(g -> dh.hasDiscovered(playerId, g).join())
                .count();
        return discovered / (double) all;
      });
    });
  }
}
