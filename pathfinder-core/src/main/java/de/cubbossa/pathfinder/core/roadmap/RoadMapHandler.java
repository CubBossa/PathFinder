package de.cubbossa.pathfinder.core.roadmap;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Objects;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class RoadMapHandler {

  private int nodeIdCounter;

  public RoadMapHandler() {


  }

  public void loadRoadMaps() {
    nodeIdCounter = NodeHandler.getInstance().getNodes().stream()
        .mapToInt(Node::getNodeId)
        .max().orElse(0);
  }

  public int requestNodeId() {
    return ++nodeIdCounter;
  }


}
