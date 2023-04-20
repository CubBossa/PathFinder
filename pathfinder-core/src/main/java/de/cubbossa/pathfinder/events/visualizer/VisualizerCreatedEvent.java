package de.cubbossa.pathfinder.events.visualizer;

import de.cubbossa.pathapi.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class VisualizerCreatedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final PathVisualizer<?, ?, ?> visualizer;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
