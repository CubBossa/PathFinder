package de.cubbossa.pathfinder.visualizer.impl;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.events.visualizer.CombinedVisualizerChangedEvent;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.tinytranslations.Message;
import dev.jorel.commandapi.arguments.Argument;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

@AutoService(VisualizerType.class)
public class CombinedVisualizerType extends AbstractVisualizerType<CombinedVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<CombinedVisualizer> {

  public CombinedVisualizerType() {
    super(AbstractPathFinder.pathfinder("combined"));
  }

  @Override
  public CombinedVisualizer create(NamespacedKey key) {
    return new CombinedVisualizer(key);
  }

  @Override
  public Message getInfoMessage(CombinedVisualizer element) {
    return Messages.CMD_VIS_COMBINED_INFO
        .insertObject("visualizer", element)
        .insertList("entries", element.getVisualizers().stream().map(Keyed::getKey).map(Objects::toString).toList());
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex,
                                       int argumentOffset) {
    return tree
        .then(Arguments.literal("add")
            .then(Arguments.pathVisualizerArgument("child")
                .executes((sender, args) -> {
                  CombinedVisualizer vis = args.getUnchecked(0);
                  PathVisualizer<?, ?> target = args.getUnchecked(1);
                  vis.addVisualizer(target);
                  Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () ->
                      Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                          CombinedVisualizerChangedEvent.Action.ADD,
                          Collections.singleton(target))));
                  AbstractPathFinder.getInstance().wrap(sender).sendMessage(Messages.CMD_VIS_COMBINED_ADD
                      .insertObject("visualizer", vis)
                      .insertObject("child", target));
                })))
        .then(Arguments.literal("remove")
            .then(Arguments.pathVisualizerArgument("child")
                .executes((sender, args) -> {
                  CombinedVisualizer vis = args.getUnchecked(0);
                  PathVisualizer<?, ?> target = args.getUnchecked(1);
                  vis.removeVisualizer(target);
                  Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () ->
                      Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                          CombinedVisualizerChangedEvent.Action.REMOVE,
                          Collections.singleton(target))));
                  AbstractPathFinder.getInstance().wrap(sender)
                      .sendMessage(Messages.CMD_VIS_COMBINED_REMOVE
                          .insertObject("visualizer", vis)
                          .insertObject("child", target));
                })))
        .then(Arguments.literal("clear")
            .executes((commandSender, args) -> {
              CombinedVisualizer vis = args.getUnchecked(0);
              Collection<PathVisualizer<?, ?>> targets = vis.getVisualizers();
              vis.clearVisualizers();
              Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () ->
                  Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
                      CombinedVisualizerChangedEvent.Action.REMOVE, targets)));
              BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_COMBINED_CLEAR
                  .insertObject("visualizer", vis));
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
