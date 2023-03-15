package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditor;
import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditorFactory;

@AutoService(NodeGroupEditorFactory.class)
public class DefaultNodeGroupEditorFactory implements NodeGroupEditorFactory {
  @Override
  public NodeGroupEditor apply(NodeGroup group) {
    return new DefaultNodeGroupEditor(group);
  }
}
