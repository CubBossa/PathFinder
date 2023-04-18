package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.api.editor.NodeGroupEditor;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;

@RequiredArgsConstructor
public class NoImplNodeGroupEditor implements NodeGroupEditor<Object> {

  @Getter
  private final NamespacedKey key;

  @Override
  public void dispose() {

  }

  @Override
  public boolean isEdited() {
    return false;
  }

  @Override
  public boolean toggleEditMode(PathPlayer<Object> player) {
    throw new IllegalStateException("Cannot use roadmap editor: no editor type registered. Are you using an API version of PathFinder?");
  }

  @Override
  public void cancelEditModes() {

  }

  @Override
  public void setEditMode(PathPlayer<Object> player, boolean activate) {
    throw new IllegalStateException("Cannot use roadmap editor: no editor type registered. Are you using an API version of PathFinder?");
  }

  @Override
  public boolean isEditing(PathPlayer<Object> uuid) {
    return false;
  }
}
