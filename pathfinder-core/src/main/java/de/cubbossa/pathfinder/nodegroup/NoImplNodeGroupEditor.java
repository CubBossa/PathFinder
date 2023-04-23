package de.cubbossa.pathfinder.nodegroup;

import de.cubbossa.pathapi.editor.NodeGroupEditor;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    throw new IllegalStateException(
        "Cannot use roadmap editor: no editor type registered. Are you using an API version of PathFinder?");
  }

  @Override
  public void cancelEditModes() {

  }

  @Override
  public void setEditMode(PathPlayer<Object> player, boolean activate) {
    throw new IllegalStateException(
        "Cannot use roadmap editor: no editor type registered. Are you using an API version of PathFinder?");
  }

  @Override
  public boolean isEditing(PathPlayer<Object> uuid) {
    return false;
  }
}
