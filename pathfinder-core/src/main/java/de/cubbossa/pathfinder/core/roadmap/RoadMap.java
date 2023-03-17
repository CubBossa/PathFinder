package de.cubbossa.pathfinder.core.roadmap;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PersistencyHolder;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
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
