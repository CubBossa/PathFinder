package de.cubbossa.pathfinder.core.node;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class Edge {

  private UUID start;
  private UUID end;
  private float weightModifier;

  private Location center;

  public Edge(UUID start, UUID end, float weightModifier) {
    this.start = start;
    this.end = end;
    this.weightModifier = weightModifier;
  }

  public Edge(Node<?> start, Node<?> end, float weightModifier) {
    this(start.getNodeId(), end.getNodeId(), weightModifier);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Edge edge)) {
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
}
