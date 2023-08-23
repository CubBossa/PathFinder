package de.cubbossa.pathfinder.editmode.clientside;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class ClientInteraction extends ClientEntity implements Interaction {

  float interactionWidth = 1;
  float interactionHeight = 1;
  boolean responsive = true;

  public ClientInteraction(PlayerSpace playerSpace, int entityId) {
    super(playerSpace, entityId, EntityType.INTERACTION);
  }

  @Override
  public void setInteractionWidth(float width) {
    if (this.interactionWidth == width) {
      return;
    }
    this.interactionWidth = width;
    metaChanged = true;
  }

  @Override
  public void setInteractionHeight(float height) {
    if (this.interactionHeight == height) {
      return;
    }
    this.interactionHeight = height;
    metaChanged = true;
  }

  @Override
  public void setResponsive(boolean response) {
    if (this.responsive == response) {
      return;
    }
    this.responsive = response;
    metaChanged = true;
  }

  @Nullable
  @Override
  public PreviousInteraction getLastAttack() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Nullable
  @Override
  public PreviousInteraction getLastInteraction() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  List<EntityData> metaData() {
    List<EntityData> data = super.metaData();
    data.add(new EntityData(8, EntityDataTypes.FLOAT, interactionWidth));
    data.add(new EntityData(9, EntityDataTypes.FLOAT, interactionHeight));
    data.add(new EntityData(10, EntityDataTypes.BOOLEAN, responsive));
    return data;
  }
}
