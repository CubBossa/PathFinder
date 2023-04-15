package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.visualizer.CombinedVisualizer;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class CombinedVisualizerChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private final CombinedVisualizer visualizer;
  private final Action action;
  private final Collection<PathVisualizer<?, ?, ?>> targets;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public enum Action {ADD, REMOVE, CLEAR}
}
