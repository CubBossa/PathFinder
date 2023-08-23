package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.NodeGroupEditor;
import de.cubbossa.pathapi.editor.NodeGroupEditorFactory;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.editmode.renderer.EdgeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.renderer.NodeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.renderer.NodeGroupListRenderer;
import de.cubbossa.pathfinder.editmode.renderer.ParticleEdgeRenderer;
import de.cubbossa.pathfinder.util.Version;
import org.bukkit.entity.Player;

@AutoService(NodeGroupEditorFactory.class)
public class DefaultNodeGroupEditorFactory implements NodeGroupEditorFactory {
  @Override
  public NodeGroupEditor<Player> apply(NodeGroup group) {
    PathFinder pathFinder = PathFinderProvider.get();
    DefaultNodeGroupEditor editor = new DefaultNodeGroupEditor(group);
    editor.getRenderers().add(new ParticleEdgeRenderer(pathFinder.getConfiguration().getEditMode()));

    try {
      if (new Version(PathFinderPlugin.getInstance().getServer().getBukkitVersion().split("-")[0]).compareTo(new Version("1.19.4")) >= 0) {
        editor.getRenderers().add(new NodeGroupListRenderer(PathFinderPlugin.getInstance(), 15, 8));
        // editor.getRenderers().add(new NodeEntityRenderer(PathFinderPlugin.getInstance()));
        // editor.getRenderers().add(new EdgeEntityRenderer(PathFinderPlugin.getInstance()));
        editor.getRenderers().add(new NodeArmorStandRenderer(PathFinderPlugin.getInstance()));
        editor.getRenderers().add(new EdgeArmorStandRenderer(PathFinderPlugin.getInstance()));
      } else {
        editor.getRenderers().add(new NodeArmorStandRenderer(PathFinderPlugin.getInstance()));
        editor.getRenderers().add(new EdgeArmorStandRenderer(PathFinderPlugin.getInstance()));
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }

    return editor;
  }
}
