package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.Storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorageUtil {

  public static Storage storage;

  public static Collection<NodeGroup> getGroups(Node node) {
    return getGroups(node.getNodeId());
  }

  public static Collection<NodeGroup> getGroups(UUID node) {
    return storage.loadGroups(node).join();
  }

  public static CompletableFuture<Void> addGroups(NodeGroup group, UUID node) {
    return addGroups(Collections.singleton(group), Collections.singleton(node));
  }

  public static CompletableFuture<Void> addGroups(NodeGroup group, Collection<UUID> nodes) {
    return addGroups(Collections.singleton(group), nodes);
  }

  public static CompletableFuture<Void> addGroups(Collection<NodeGroup> groups, UUID node) {
    return addGroups(groups, Collections.singleton(node));
  }

  public static CompletableFuture<Void> addGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {
    return CompletableFuture.allOf((CompletableFuture<?>) groups.stream()
        .peek(group -> group.addAll(nodes))
        .map(storage::saveGroup)
        .toList()
    );
  }

  public static CompletableFuture<Void> removeGroups(NodeGroup group, UUID node) {
    return removeGroups(Collections.singleton(group), Collections.singleton(node));
  }

  public static CompletableFuture<Void> removeGroups(NodeGroup group, Collection<UUID> nodes) {
    return removeGroups(Collections.singleton(group), nodes);
  }

  public static CompletableFuture<Void> removeGroups(Collection<NodeGroup> groups, UUID node) {
    return removeGroups(groups, Collections.singleton(node));
  }

  public static CompletableFuture<Void> removeGroups(Collection<NodeGroup> groups, Collection<UUID> nodes) {
    return CompletableFuture.allOf((CompletableFuture<?>) groups.stream()
        .peek(group -> group.removeAll(nodes))
        .map(storage::saveGroup)
        .toList()
    );
  }

  public static CompletableFuture<Void> clearGroups(Node... node) {
    return clearGroups(Set.of(node).stream().map(Node::getNodeId).toList());
  }

  public static CompletableFuture<Void> clearGroups(UUID... node) {
    return clearGroups(Set.of(node));
  }

  public static CompletableFuture<Void> clearGroups(Collection<UUID> nodes) {
    return CompletableFuture.supplyAsync(() -> nodes.stream()
        .map(storage::loadGroups)
        .map(CompletableFuture::join)
        .flatMap(Collection::stream)
        .toList()
    ).thenAccept(groups -> {
      groups.forEach(group -> group.removeAll(nodes));
      groups.stream()
          .peek(group -> group.removeAll(nodes))
          .forEach(storage::saveGroup);
    });
  }


}
