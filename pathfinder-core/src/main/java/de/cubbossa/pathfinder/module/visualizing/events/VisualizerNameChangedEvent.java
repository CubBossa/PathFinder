package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class VisualizerNameChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final PathVisualizer<?, ?, ?> visualizer;
  private final String oldNameFormat;
  private final String newNameFormat;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
