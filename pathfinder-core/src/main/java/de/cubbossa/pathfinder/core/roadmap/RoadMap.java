package de.cubbossa.pathfinder.core.roadmap;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PersistencyHolder;
import de.cubbossa.pathfinder.core.events.node.EdgesCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.NavigateSelection;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.data.DataStorageException;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.location.LocationWeightSolver;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.location.LocationWeightSolverPreset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
class RoadMap implements Keyed, Named, PersistencyHolder {

  private final NamespacedKey key;
  private final boolean persistent;
  private String nameFormat;
  private Component displayName;
  private double defaultCurveLength;
  private PathVisualizer<?, ?> visualizer;

  public RoadMap(NamespacedKey key, String name, PathVisualizer<?, ?> visualizer,
                 double defaultCurveLength) {
    this(key, name, visualizer, defaultCurveLength, true);
  }

  public RoadMap(NamespacedKey key, String name, PathVisualizer<?, ?> visualizer,
                 double defaultCurveLength, boolean persistent) {

    this.key = key;
    this.persistent = persistent;
    this.setNameFormat(name);
    this.defaultCurveLength = defaultCurveLength;

    this.nodes = new TreeMap<>();
    this.edges = new HashSet<>();

    setVisualizer(visualizer);
  }

  public void setNameFormat(String nameFormat) {
    this.nameFormat = nameFormat;
    this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
  }


  public RoadMapBatchEditor getBatchEditor() {
    AtomicBoolean closed = new AtomicBoolean(false);

    return new RoadMapBatchEditor() {
      @Override
      public <T extends Node<T>> void createNode(NodeType<T> type, Vector vector, String permission,
                                                 NodeGroup... groups) {
        if (closed.get()) {
          throw new IllegalStateException("Batch Editor already closed.");
        }
      }

      @Override
      public void commit() {
        if (closed.get()) {
          throw new IllegalStateException("Batch Editor already closed.");
        }


        closed.set(true);
      }
    };
  }

  public interface RoadMapBatchEditor {

    <T extends Node<T>> void createNode(NodeType<T> type, Vector vector, String permission,
                                        NodeGroup... groups);

    void commit();
  }
}
