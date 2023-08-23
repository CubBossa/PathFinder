package de.cubbossa.pathfinder.editmode.clientside;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ClientBlockDisplay extends ClientDisplay implements BlockDisplay {

  BlockData block = Material.AIR.createBlockData();

  public ClientBlockDisplay(PlayerSpace playerSpace, int entityId) {
    super(playerSpace, entityId, EntityType.BLOCK_DISPLAY);
  }

  @Override
  public void setBlock(@NotNull BlockData block) {
    if (this.block.equals(block)) {
      return;
    }
    this.block = block;
    metaChanged = true;
  }

  @Override
  List<EntityData> metaData() {
    List<EntityData> data = super.metaData();
    data.add(new EntityData(22, EntityDataTypes.BLOCK_STATE, SpigotConversionUtil.fromBukkitBlockData(block).getGlobalId()));
    return data;
  }
}
