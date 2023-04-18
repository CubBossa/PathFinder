package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.editor.NodeGroupEditor;
import de.cubbossa.pathfinder.api.editor.NodeGroupEditorFactory;
import de.cubbossa.pathfinder.editmode.renderer.NodeArmorStandRenderer;
import de.cubbossa.pathfinder.editmode.renderer.ParticleEdgeRenderer;
import org.bukkit.entity.Player;

@AutoService(NodeGroupEditorFactory.class)
public class DefaultNodeGroupEditorFactory implements NodeGroupEditorFactory {
  @Override
  public NodeGroupEditor<Player> apply(NodeGroup group) {
    DefaultNodeGroupEditor editor = new DefaultNodeGroupEditor(group);
    editor.getRenderers().add(new ParticleEdgeRenderer());
    editor.getRenderers().add(new NodeArmorStandRenderer(PathPlugin.getInstance()));
    return editor;
  }
}
