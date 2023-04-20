package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class SimpleEdge implements Edge {

  private final PathFinder pathFinder;

  private UUID start;
  private UUID end;
  private float weightModifier;

  private Location center;

  public SimpleEdge(UUID start, UUID end, float weightModifier) {
    this.pathFinder = PathFinderProvider.get();
    this.start = start;
    this.end = end;
    this.weightModifier = weightModifier;
  }

  public SimpleEdge(Node<?> start, Node<?> end, float weightModifier) {
    this(start.getNodeId(), end.getNodeId(), weightModifier);
  }

  public CompletableFuture<Node<?>> resolveStart() {
    return pathFinder.getStorage().loadNode(start).thenApply(Optional::orElseThrow);
  }

  public CompletableFuture<Node<?>> resolveEnd() {
    return pathFinder.getStorage().loadNode(end).thenApply(Optional::orElseThrow);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleEdge edge)) {
      return false;
    }

    if (Float.compare(edge.weightModifier, weightModifier) != 0) {
      return false;
    }
    if (!Objects.equals(start, edge.start)) {
      return false;
    }
    return Objects.equals(end, edge.end);
  }

  @Override
  public int hashCode() {
    int result = start != null ? start.hashCode() : 0;
    result = 31 * result + (end != null ? end.hashCode() : 0);
    result = 31 * result + (weightModifier != +0.0f ? Float.floatToIntBits(weightModifier) : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Edge{" +
        "start=" + start +
        ", end=" + end +
        ", weightModifier=" + weightModifier +
        '}';
  }

  @Override
  public float getWeight() {
    return weightModifier;
  }
}
