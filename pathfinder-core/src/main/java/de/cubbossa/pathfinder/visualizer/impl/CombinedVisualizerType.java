package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.events.visualizer.CombinedVisualizerChangedEvent;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.Argument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class CombinedVisualizerType extends InternalVisualizerType<CombinedVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<CombinedVisualizer> {

  public CombinedVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public CombinedVisualizer create(NamespacedKey key, String nameFormat) {
    return new CombinedVisualizer(key, nameFormat);
  }

  @Override
  public Message getInfoMessage(CombinedVisualizer element) {
    return Messages.CMD_VIS_COMBINED_INFO.formatted(
        TagResolver.resolver("entries", Messages.formatList(
            element.getVisualizers(),
            v -> v == null ? Component.text("undefined")
                : v.getDisplayName() != null ? v.getDisplayName()
                : Component.text(v.getNameFormat()))
        )
    );
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex,
                                       int argumentOffset) {
    return tree
        .then(CustomArgs.literal("add")
            .then(CustomArgs.pathVisualizerArgument("child")
                .executes((sender, args) -> {
                  CombinedVisualizer vis = args.getUnchecked(0);
                  PathVisualizer<?, ?> target = args.getUnchecked(1);
                  vis.addVisualizer(target);
                  Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () ->
                      Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                          CombinedVisualizerChangedEvent.Action.ADD,
                          Collections.singleton(target))));
                  BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_COMBINED_ADD.formatted(
                      Placeholder.component("visualizer", vis.getDisplayName()),
                      Placeholder.component("child", target.getDisplayName())
                  ));
                })))
        .then(CustomArgs.literal("remove")
            .then(CustomArgs.pathVisualizerArgument("child")
                .executes((sender, args) -> {
                  CombinedVisualizer vis = args.getUnchecked(0);
                  PathVisualizer<?, ?> target = args.getUnchecked(1);
                  vis.removeVisualizer(target);
                  Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () ->
                      Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                          CombinedVisualizerChangedEvent.Action.REMOVE,
                          Collections.singleton(target))));
                  BukkitUtils.wrap(sender)
                      .sendMessage(Messages.CMD_VIS_COMBINED_REMOVE.formatted(
                          Placeholder.component("visualizer", vis.getDisplayName()),
                          Placeholder.component("child", target.getDisplayName())
                      ));
                })))
        .then(CustomArgs.literal("clear")
            .executes((commandSender, args) -> {
              CombinedVisualizer vis = args.getUnchecked(0);
              Collection<PathVisualizer<?, ?>> targets = vis.getVisualizers();
              vis.clearVisualizers();
              Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () ->
                  Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                      CombinedVisualizerChangedEvent.Action.REMOVE, targets)));
              BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_COMBINED_CLEAR.formatted(
                  Placeholder.component("visualizer", vis.getDisplayName())
              ));
            }));
  }

  @Override
  public Map<String, Object> serialize(CombinedVisualizer visualizer) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("children", visualizer.getVisualizers().stream()
        .map(PathVisualizer::getKey)
        .map(NamespacedKey::toString)
        .collect(Collectors.toList()));
    return map;
  }

  @Override
  public void deserialize(CombinedVisualizer visualizer, Map<String, Object> values) {
    List<String> val = (List<String>) values.get("children");
    if (val == null) {
      return;
    }
    val.stream()
        .map(NamespacedKey::fromString)
        .forEach(visualizer::addVisualizer);
  }
}
