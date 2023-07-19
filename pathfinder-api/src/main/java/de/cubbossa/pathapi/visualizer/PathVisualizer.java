package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.PermissionHolder;
import de.cubbossa.pathapi.node.Node;

import java.util.List;

public interface PathVisualizer<ViewT extends PathView<PlayerT>, PlayerT> extends Keyed, PermissionHolder {

  Class<PlayerT> getTargetType();

  ViewT createView(List<Node> nodes, PathPlayer<PlayerT> player);
}
