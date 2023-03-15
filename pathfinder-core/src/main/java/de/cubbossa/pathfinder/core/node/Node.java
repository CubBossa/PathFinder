package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.PersistencyHolder;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Node<N extends Node<N>> extends Navigable, PersistencyHolder, Comparable<Node<?>> {

  int getNodeId();

  NodeType<N> getType();

  Location getLocation();

  void setLocation(Location location);

  Collection<Edge> getEdges();

  @Nullable Double getCurveLength();

  void setCurveLength(Double value);

  Edge connect(Node<?> target);

  void disconnect(Node<?> target);

  @Override
  default int compareTo(@NotNull Node<?> o) {
    return Integer.compare(getNodeId(), o.getNodeId());
  }
}
