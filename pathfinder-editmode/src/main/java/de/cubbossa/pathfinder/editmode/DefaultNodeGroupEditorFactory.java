package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.group.NodeGroupEditor;
import de.cubbossa.pathfinder.api.group.NodeGroupEditorFactory;
import org.bukkit.entity.Player;

@AutoService(NodeGroupEditorFactory.class)
public class DefaultNodeGroupEditorFactory implements NodeGroupEditorFactory {
  @Override
  public NodeGroupEditor<Player> apply(NodeGroup group) {
    return new DefaultNodeGroupEditor(group);
  }
}
