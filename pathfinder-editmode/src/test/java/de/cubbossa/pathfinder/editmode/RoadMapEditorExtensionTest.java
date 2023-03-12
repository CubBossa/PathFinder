package de.cubbossa.pathfinder.editmode;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.plugin.UnknownDependencyException;
import org.junit.jupiter.api.Test;

class RoadMapEditorExtensionTest {

  @Test
  public void checkVersion1() {
    RoadMapEditorExtension roadMapEditorExtension = new RoadMapEditorExtension();
    assertThrows(UnknownDependencyException.class, () -> roadMapEditorExtension.checkVersion("1.19", "4.8"));
  }

  @Test
  public void checkVersion2() {
    RoadMapEditorExtension roadMapEditorExtension = new RoadMapEditorExtension();
    assertThrows(UnknownDependencyException.class, () -> roadMapEditorExtension.checkVersion("1.19.1", "5.0.0-b700"));
  }

  @Test
  public void checkVersion3() {
    RoadMapEditorExtension roadMapEditorExtension = new RoadMapEditorExtension();
    assertThrows(UnknownDependencyException.class, () -> roadMapEditorExtension.checkVersion("1.19.3", "5.0.0-b120"));
  }

  @Test
  public void checkVersion4() {
    RoadMapEditorExtension roadMapEditorExtension = new RoadMapEditorExtension();
    assertDoesNotThrow(() -> roadMapEditorExtension.checkVersion("1.19.3", "5.0.0-SNAPSHOT-b614"));
  }
}