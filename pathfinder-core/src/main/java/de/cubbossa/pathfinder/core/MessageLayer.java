package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.TranslationHandler;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

public class MessageLayer extends PassLayer implements ApplicationLayer {

  private final CommandSender sender;
  private final ApplicationLayer subLayer;

  public MessageLayer(CommandSender sender, ApplicationLayer subLayer) {
    super(subLayer);
    this.sender = sender;
    this.subLayer = subLayer;
  }

  public EventsLayer eventLayer() {
    return new EventsLayer(this);
  }

  @Override
  public <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type, Location location) {
    return subLayer.createNode(type, location).thenApply(n -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CREATE.format(
              TagResolver.resolver("id", Tag.inserting(Component.text(n.getNodeId().toString())))),
          sender);
      return n;
    });
  }

  @Override
  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {
    return subLayer.deleteNodes(nodes).thenApply(unused -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DELETE.format(
              Placeholder.component("selection", Messages.formatNodeSelection(sender, nodes))),
          sender);
      return unused;
    });
  }

  @Override
  public CompletableFuture<Void> updateNodes(NodeSelection nodes, Consumer<Node<?>> nodeConsumer) {
    return subLayer.updateNodes(nodes, nodeConsumer).thenApply(unused -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_N_UPDATED.format(TagResolver.builder()
          .resolver(Placeholder.component("selection", Messages.formatNodeSelection(sender, nodes)))
          .build()), sender);
      return unused;
    });
  }

  @Override
  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
    return subLayer.createNodeGroup(key)
        .thenApply(group -> {
          TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(
              Placeholder.parsed("key", group.getKey().toString())
          ), sender);
          return group;
        })
        .exceptionally(e -> {
          if (e instanceof IllegalArgumentException) {
            TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS.format(
                Placeholder.parsed("name", key.toString())
            ), sender);
            return null;
          }
          TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE_FAIL, sender);
          e.printStackTrace();
          return null;
        });
  }

  @Override
  public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
    return subLayer.getNodeGroups(pagination).thenApply(nodeGroups -> {
      CommandUtils.printList(
          sender,
          pagination,
          this::getNodeGroups,
          group -> {
            TagResolver r = TagResolver.builder()
                .resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
                .resolver(Placeholder.component("size", Component.text(group.size())))
                .build();
            TranslationHandler.getInstance()
                .sendMessage(Messages.CMD_NG_LIST_LINE.format(r), sender);
          },
          Messages.CMD_NG_LIST_HEADER,
          Messages.CMD_NG_LIST_FOOTER
      );
      return nodeGroups;
    });
  }

  @Override
  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
    return subLayer.deleteNodeGroup(key).thenApply(unused -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(
          Placeholder.parsed("name", key.toString())
      ), sender);
      return unused;
    });
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroup(NamespacedKey group, NodeSelection selection) {
    return subLayer.assignNodesToGroup(group, selection).thenApply(unused -> {
      TranslationHandler.getInstance()
          .sendMessage(Messages.CMD_N_ADD_GROUP.format(TagResolver.builder()
              .resolver(
                  Placeholder.component("nodes", Messages.formatNodeSelection(sender, selection)))
              .build()), sender);
      return unused;
    });
  }

  @Override
  public CompletableFuture<Void> removeNodesFromGroup(NamespacedKey group,
                                                      NodeSelection selection) {
    return subLayer.removeNodesFromGroup(group, selection).thenApply(unused -> {
      TranslationHandler.getInstance()
          .sendMessage(Messages.CMD_N_REMOVE_GROUP.format(TagResolver.builder()
              .resolver(
                  Placeholder.component("nodes", Messages.formatNodeSelection(sender, selection)))
              .build()), sender);
      return unused;
    });
  }

  @Override
  public CompletableFuture<Void> clearNodeGroups(NodeSelection selection) {
    return subLayer.clearNodeGroups(selection).thenApply(unused -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CLEAR_GROUPS.format(
          TagResolver.builder()
              .resolver(
                  Placeholder.component("nodes", Messages.formatNodeSelection(sender, selection)))
              .build()), sender);
      return unused;
    });
  }
}
