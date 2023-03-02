package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapEditor;
import de.cubbossa.pathfinder.core.roadmap.RoadMapEditorFactory;

@AutoService(RoadMapEditorFactory.class)
public class DefaultRoadMapEditorFactory implements RoadMapEditorFactory {
  @Override
  public RoadMapEditor apply(RoadMap roadMap) {
    return new DefaultRoadMapEditor(roadMap);
  }
}
