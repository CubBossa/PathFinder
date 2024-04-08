package de.cubbossa.pathfinder.editmode;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.editor.GraphEditor;
import de.cubbossa.pathfinder.editor.GraphEditorFactory;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.editmode.renderer.EdgeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.renderer.EdgeEntityRenderer;
import de.cubbossa.pathfinder.editmode.renderer.NodeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.renderer.NodeEntityRenderer;
import de.cubbossa.pathfinder.editmode.renderer.NodeGroupListRenderer;
import de.cubbossa.pathfinder.editmode.renderer.ParticleEdgeRenderer;
import de.cubbossa.pathfinder.util.Version;
import org.bukkit.entity.Player;
import org.pf4j.Extension;

@Extension(points = GraphEditorFactory.class)
public class DefaultGraphEditorFactory implements GraphEditorFactory {

  @Override
  public GraphEditor<Player> createGraphEditor(NodeGroup group) {
    PathFinder pathFinder = PathFinderProvider.get();
    DefaultGraphEditor editor = new DefaultGraphEditor(group);

    editor.getRenderers().add(new ParticleEdgeRenderer(pathFinder.getConfiguration().getEditMode()));
    try {
      if (new Version(PathFinderPlugin.getInstance().getServer().getBukkitVersion().split("-")[0]).compareTo(new Version("1.19.4")) >= 0) {
        editor.addRenderer(new NodeGroupListRenderer(PathFinderPlugin.getInstance(), 15, 8));
        editor.addRenderer(new NodeEntityRenderer(PathFinderPlugin.getInstance()));
        editor.addRenderer(new EdgeEntityRenderer(PathFinderPlugin.getInstance()));
      } else {
        editor.addRenderer(new NodeArmorStandRenderer(PathFinderPlugin.getInstance()));
        editor.addRenderer(new EdgeArmorStandRenderer(PathFinderPlugin.getInstance()));
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }

    return editor;
  }
}
