package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.commands.CustomArgs;
import de.cubbossa.pathfinder.commands.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.commands.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.events.visualizer.CombinedVisualizerChangedEvent;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTree;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

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
    return Messages.CMD_VIS_COMBINED_INFO.format(
        TagResolver.resolver("entries", Messages.formatList(
            element.getVisualizers(),
            v -> v == null ? Component.text("undefined")
                : v.getDisplayName() != null ? v.getDisplayName()
                    : Component.text(v.getNameFormat()))
        )
    );
  }

  @Override
  public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex,
                                        int argumentOffset) {
    return tree
        .then(CustomArgs.literal("add")
            .then(CustomArgs.pathVisualizerArgument("child")
                .executes((sender, objects) -> {
                  CombinedVisualizer vis = (CombinedVisualizer) objects[0];
                  PathVisualizer<?, ?> target = (PathVisualizer<?, ?>) objects[1];
                  vis.addVisualizer(target);
                  Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () ->
                      Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                          CombinedVisualizerChangedEvent.Action.ADD,
                          Collections.singleton(target))));
                  TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_COMBINED_ADD.format(
                      Placeholder.component("visualizer", vis.getDisplayName()),
                      Placeholder.component("child", target.getDisplayName())
                  ), sender);
                })))
        .then(CustomArgs.literal("remove")
            .then(CustomArgs.pathVisualizerArgument("child")
                .executes((sender, objects) -> {
                  CombinedVisualizer vis = (CombinedVisualizer) objects[0];
                  PathVisualizer<?, ?> target = (PathVisualizer<?, ?>) objects[1];
                  vis.removeVisualizer(target);
                  Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () ->
                      Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                          CombinedVisualizerChangedEvent.Action.REMOVE,
                          Collections.singleton(target))));
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.CMD_VIS_COMBINED_REMOVE.format(
                          Placeholder.component("visualizer", vis.getDisplayName()),
                          Placeholder.component("child", target.getDisplayName())
                      ), sender);
                })))
        .then(CustomArgs.literal("clear")
            .executes((commandSender, objects) -> {
              CombinedVisualizer vis = (CombinedVisualizer) objects[0];
              Collection<PathVisualizer<?, ?>> targets = vis.getVisualizers();
              vis.clearVisualizers();
              Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () ->
                  Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                      CombinedVisualizerChangedEvent.Action.REMOVE, targets)));
              TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_COMBINED_CLEAR.format(
                  Placeholder.component("visualizer", vis.getDisplayName())
              ), commandSender);
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
