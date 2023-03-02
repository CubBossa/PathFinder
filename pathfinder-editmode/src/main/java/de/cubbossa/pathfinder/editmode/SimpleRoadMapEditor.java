package de.cubbossa.pathfinder.editmode;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.core.roadmap.RoadMapEditor;

@AutoService(RoadMapEditor.class)
public class SimpleRoadMapEditor implements RoadMapEditor {

  @Override
  public void printTest() {
    System.out.println("From Editor".repeat(20));
  }
}
