package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class VisualizerPropertyChangedEvent<T> extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final PathVisualizer<?, ?> visualizer;
  private final String field;
  private final boolean visual;
  private final T oldValue;
  private final T newValue;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
