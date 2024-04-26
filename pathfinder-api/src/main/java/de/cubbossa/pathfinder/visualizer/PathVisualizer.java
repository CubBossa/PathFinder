package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.Keyed;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.misc.PermissionHolder;
import de.cubbossa.pathfinder.navigation.UpdatingPath;

public interface PathVisualizer<ViewT extends PathView<PlayerT>, PlayerT> extends Keyed, PermissionHolder, Disposable {

  Class<PlayerT> getTargetType();

  ViewT createView(UpdatingPath nodes, PathPlayer<PlayerT> player);
}
