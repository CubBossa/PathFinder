package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.DiscoverProgressModifier;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathfinder.module.AbstractDiscoverHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class CommonDiscoverProgressModifier implements DiscoverProgressModifier {

  private final NamespacedKey ownerGroup;
  private final String nameFormat;
  private final String commandKey;

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
    Storage storage = PathFinderProvider.get().getStorage();
    AbstractDiscoverHandler<Player> dh = AbstractDiscoverHandler.getInstance();

    return storage.loadGroup(ownerGroup).thenCompose(group -> {
      return storage.loadGroups(group.orElseThrow()).thenApply(m -> {
        Collection<NodeGroup> discoverableGroupsWithin = m.values().stream()
            .flatMap(Collection::stream)
            .distinct()
            .filter(g -> g.hasModifier(DiscoverableModifier.KEY))
            .toList();
        int all = discoverableGroupsWithin.size();
        int discovered = (int) discoverableGroupsWithin.stream()
            .filter(g -> dh.hasDiscovered(playerId, g).join())
            .count();
        return discovered / (double) all;
      });
    });
  }
}
