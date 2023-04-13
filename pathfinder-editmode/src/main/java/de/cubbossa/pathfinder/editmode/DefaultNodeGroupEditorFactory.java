package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupEditor;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupEditorFactory;

@AutoService(NodeGroupEditorFactory.class)
public class DefaultNodeGroupEditorFactory implements NodeGroupEditorFactory {
  @Override
  public NodeGroupEditor apply(SimpleNodeGroup group) {
    return new DefaultNodeGroupEditor(group);
  }
}
