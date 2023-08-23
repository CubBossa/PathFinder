package de.cubbossa.pathfinder.editmode.clientside;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import lombok.Getter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ClientArmorStand extends ClientLivingEntity implements ArmorStand {

  private boolean small = false;
  private boolean basePlate = true;
  private boolean marker = false;
  private boolean arms = false;

  private EulerAngle headPose = new EulerAngle(0, 0, 0);
  private EulerAngle bodyPose = new EulerAngle(0, 0, 0);
  private EulerAngle rightArmPose = new EulerAngle(0, 0, 0);
  private EulerAngle leftArmPose = new EulerAngle(0, 0, 0);
  private EulerAngle rightLegPose = new EulerAngle(0, 0, 0);
  private EulerAngle leftLegPose = new EulerAngle(0, 0, 0);

  private Map<EquipmentSlot, LockType> equipmentSlotLockTypeMap;


  public ClientArmorStand(PlayerSpace playerSpace, int entityId) {
    super(playerSpace, entityId, EntityType.ARMOR_STAND);
    equipmentSlotLockTypeMap = new HashMap<>();
  }

  @NotNull
  @Override
  public ItemStack getItemInHand() {
    return equipment.getItemInMainHand();
  }

  @Override
  public void setItemInHand(@Nullable ItemStack item) {
    equipment.setItemInMainHand(item);
  }

  @NotNull
  @Override
  public ItemStack getBoots() {
    return equipment.getBoots();
  }

  @Override
  public void setBoots(@Nullable ItemStack item) {
    equipment.setBoots(item);
  }

  @NotNull
  @Override
  public ItemStack getLeggings() {
    return equipment.getLeggings();
  }

  @Override
  public void setLeggings(@Nullable ItemStack item) {
    equipment.setLeggings(item);
  }

  @NotNull
  @Override
  public ItemStack getChestplate() {
    return equipment.getChestplate();
  }

  @Override
  public void setChestplate(@Nullable ItemStack item) {
    equipment.setChestplate(item);
  }

  @NotNull
  @Override
  public ItemStack getHelmet() {
    return equipment.getHelmet();
  }

  @Override
  public void setHelmet(@Nullable ItemStack item) {
    equipment.setHelmet(item);
  }

  @Override
  public void setBodyPose(@NotNull EulerAngle pose) {
    bodyPose = pose;
    metaChanged = true;
  }

  @Override
  public void setLeftArmPose(@NotNull EulerAngle pose) {
    leftArmPose = pose;
    metaChanged = true;
  }

  @Override
  public void setRightArmPose(@NotNull EulerAngle pose) {
    rightArmPose = pose;
    metaChanged = true;
  }

  @Override
  public void setLeftLegPose(@NotNull EulerAngle pose) {
    leftLegPose = pose;
    metaChanged = true;
  }

  @Override
  public void setRightLegPose(@NotNull EulerAngle pose) {
    rightLegPose = pose;
    metaChanged = true;
  }

  @Override
  public void setHeadPose(@NotNull EulerAngle pose) {
    headPose = pose;
    metaChanged = true;
  }

  @Override
  public boolean hasArms() {
    return arms;
  }

  @Override
  public void setArms(boolean arms) {
    this.arms = arms;
    metaChanged = true;
  }

  @Override
  public void setSmall(boolean small) {
    this.small = small;
    metaChanged = true;
  }

  @Override
  public void setMarker(boolean marker) {
    this.marker = marker;
    metaChanged = true;
  }

  @Override
  public boolean hasBasePlate() {
    return basePlate;
  }

  @Override
  public void setBasePlate(boolean basePlate) {
    this.basePlate = basePlate;
    metaChanged = true;
  }

  @Override
  public void setVisible(boolean visible) {
    this.visible = visible;
    metaChanged = true;
  }

  @Override
  public void addEquipmentLock(@NotNull EquipmentSlot slot, @NotNull ArmorStand.LockType lockType) {
    equipmentSlotLockTypeMap.put(slot, lockType);
  }

  @Override
  public void removeEquipmentLock(@NotNull EquipmentSlot slot, @NotNull ArmorStand.LockType lockType) {
    equipmentSlotLockTypeMap.remove(slot);
  }

  @Override
  public boolean hasEquipmentLock(@NotNull EquipmentSlot slot, @NotNull ArmorStand.LockType lockType) {
    return equipmentSlotLockTypeMap.get(slot).equals(lockType);
  }


  @Override
  List<EntityData> metaData() {
    List<EntityData> data = super.metaData();
    byte mask = (byte)
        ((isSmall() ? 0x01 : 0)
            | (hasArms() ? 0x02 : 0)
            | (!hasBasePlate() ? 0x04 : 0)
            | (isMarker() ? 0x08 : 0));
    if (mask != 0) {
      data.add(new EntityData(15, EntityDataTypes.BYTE, mask));
    }
    if (!headPose.equals(new EulerAngle(0, 0, 0))) {
      data.add(new EntityData(16, EntityDataTypes.ROTATION,
          new Vector3f((float) headPose.getX(), (float) headPose.getY(), (float) headPose.getZ())));
    }
    if (!bodyPose.equals(new EulerAngle(0, 0, 0))) {
      data.add(new EntityData(17, EntityDataTypes.ROTATION,
          new Vector3f((float) bodyPose.getX(), (float) bodyPose.getY(), (float) bodyPose.getZ())));
    }
    if (!leftArmPose.equals(new EulerAngle(-10, 0, 10))) {
      data.add(new EntityData(18, EntityDataTypes.ROTATION,
          new Vector3f((float) leftArmPose.getX(), (float) leftArmPose.getY(), (float) leftArmPose.getZ())));
    }
    if (!rightArmPose.equals(new EulerAngle(-15, 0, 10))) {
      data.add(new EntityData(19, EntityDataTypes.ROTATION,
          new Vector3f((float) rightArmPose.getX(), (float) rightArmPose.getY(), (float) rightArmPose.getZ())));
    }
    if (!leftLegPose.equals(new EulerAngle(-1, 0, -1))) {
      data.add(new EntityData(20, EntityDataTypes.ROTATION,
          new Vector3f((float) leftLegPose.getX(), (float) leftLegPose.getY(), (float) leftLegPose.getZ())));
    }
    if (!rightLegPose.equals(new EulerAngle(1, 0, 1))) {
      data.add(new EntityData(21, EntityDataTypes.ROTATION,
          new Vector3f((float) rightLegPose.getX(), (float) rightLegPose.getY(), (float) rightLegPose.getZ())));
    }
    return data;
  }
}
