package de.cubbossa.pathfinder.editmode.clientside;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class ClientLivingEntity extends ClientDamageable implements LivingEntity {

  EntityEquipment equipment = new ClientEntityEquipment(this);

  // change list
  boolean equipmentChanged = false;

  public ClientLivingEntity(PlayerSpace playerSpace, int entityId, EntityType entityType) {
    super(playerSpace, entityId, entityType);
  }

  @Override
  public double getEyeHeight() {
    return 0;
  }

  @Override
  public double getEyeHeight(boolean ignorePose) {
    return 0;
  }

  @NotNull
  @Override
  public Location getEyeLocation() {
    return null;
  }

  @NotNull
  @Override
  public List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
    return null;
  }

  @NotNull
  @Override
  public Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
    return null;
  }

  @NotNull
  @Override
  public List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
    return null;
  }

  @Nullable
  @Override
  public Block getTargetBlockExact(int maxDistance) {
    return null;
  }

  @Nullable
  @Override
  public Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
    return null;
  }

  @Nullable
  @Override
  public RayTraceResult rayTraceBlocks(double maxDistance) {
    return null;
  }

  @Nullable
  @Override
  public RayTraceResult rayTraceBlocks(double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
    return null;
  }

  @Override
  public int getRemainingAir() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setRemainingAir(int ticks) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public int getMaximumAir() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setMaximumAir(int ticks) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public int getArrowCooldown() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setArrowCooldown(int ticks) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public int getArrowsInBody() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setArrowsInBody(int count) {

  }

  @Override
  public int getMaximumNoDamageTicks() {
    return 0;
  }

  @Override
  public void setMaximumNoDamageTicks(int ticks) {

  }

  @Override
  public double getLastDamage() {
    return 0;
  }

  @Override
  public void setLastDamage(double damage) {

  }

  @Override
  public int getNoDamageTicks() {
    return 0;
  }

  @Override
  public void setNoDamageTicks(int ticks) {

  }

  @Nullable
  @Override
  public Player getKiller() {
    return null;
  }

  @Override
  public boolean addPotionEffect(@NotNull PotionEffect effect) {
    return false;
  }

  @Override
  public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
    return false;
  }

  @Override
  public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
    return false;
  }

  @Override
  public boolean hasPotionEffect(@NotNull PotionEffectType type) {
    return false;
  }

  @Nullable
  @Override
  public PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
    return null;
  }

  @Override
  public void removePotionEffect(@NotNull PotionEffectType type) {

  }

  @NotNull
  @Override
  public Collection<PotionEffect> getActivePotionEffects() {
    return null;
  }

  @Override
  public boolean hasLineOfSight(@NotNull Entity other) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean getRemoveWhenFarAway() {
    return false;
  }

  @Override
  public void setRemoveWhenFarAway(boolean remove) {

  }

  @Override
  public void setCanPickupItems(boolean pickup) {

  }

  @Override
  public boolean getCanPickupItems() {
    return false;
  }

  @Override
  public boolean isLeashed() {
    return false;
  }

  @NotNull
  @Override
  public Entity getLeashHolder() throws IllegalStateException {
    return null;
  }

  @Override
  public boolean setLeashHolder(@Nullable Entity holder) {
    return false;
  }

  @Override
  public boolean isGliding() {
    return false;
  }

  @Override
  public void setGliding(boolean gliding) {

  }

  @Override
  public boolean isSwimming() {
    return false;
  }

  @Override
  public void setSwimming(boolean swimming) {

  }

  @Override
  public boolean isRiptiding() {
    return false;
  }

  @Override
  public boolean isSleeping() {
    return false;
  }

  @Override
  public boolean isClimbing() {
    return false;
  }

  @Override
  public void setAI(boolean ai) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean hasAI() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void attack(@NotNull Entity target) {

  }

  @Override
  public void swingMainHand() {

  }

  @Override
  public void swingOffHand() {

  }

  @Override
  public void setCollidable(boolean collidable) {

  }

  @Override
  public boolean isCollidable() {
    return false;
  }

  @NotNull
  @Override
  public Set<UUID> getCollidableExemptions() {
    return null;
  }

  @Nullable
  @Override
  public <T> T getMemory(@NotNull MemoryKey<T> memoryKey) {
    return null;
  }

  @Override
  public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {

  }

  @Nullable
  @Override
  public Sound getHurtSound() {
    return null;
  }

  @Nullable
  @Override
  public Sound getDeathSound() {
    return null;
  }

  @NotNull
  @Override
  public Sound getFallDamageSound(int fallHeight) {
    return null;
  }

  @NotNull
  @Override
  public Sound getFallDamageSoundSmall() {
    return null;
  }

  @NotNull
  @Override
  public Sound getFallDamageSoundBig() {
    return null;
  }

  @NotNull
  @Override
  public Sound getDrinkingSound(@NotNull ItemStack itemStack) {
    return null;
  }

  @NotNull
  @Override
  public Sound getEatingSound(@NotNull ItemStack itemStack) {
    return null;
  }

  @Override
  public boolean canBreatheUnderwater() {
    return false;
  }

  @NotNull
  @Override
  public EntityCategory getCategory() {
    return null;
  }

  @Override
  public void setInvisible(boolean invisible) {

  }

  @Override
  public boolean isInvisible() {
    return false;
  }

  @Nullable
  @Override
  public AttributeInstance getAttribute(@NotNull Attribute attribute) {
    return null;
  }

  @NotNull
  @Override
  public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile) {
    return null;
  }

  @NotNull
  @Override
  public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile, @Nullable Vector velocity) {
    return null;
  }

  @Override
  public void update(Collection<Player> viewers) {
    super.update(viewers);
    PacketEventsAPI<?> api = PacketEvents.getAPI();
    for (Player player : viewers) {
      if (equipmentChanged) {

        List<Equipment> equip = new ArrayList<>();
        equip.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(equipment.getBoots())));
        equip.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(equipment.getLeggings())));
        equip.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(equipment.getChestplate())));
        equip.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(equipment.getHelmet())));
        equip.add(new Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(equipment.getItemInMainHand())));
        equip.add(new Equipment(EquipmentSlot.OFF_HAND, SpigotConversionUtil.fromBukkitItemStack(equipment.getItemInOffHand())));

        api.getPlayerManager().sendPacket(player, new WrapperPlayServerEntityEquipment(entityId, equip));
        equipmentChanged = false;
      }
    }
  }

  @Override
  List<EntityData> metaData() {
    List<EntityData> data = super.metaData();
    data.add(new EntityData(9, EntityDataTypes.FLOAT, (float) getHealth()));
    data.add(new EntityData(10, EntityDataTypes.INT, 0));
    data.add(new EntityData(11, EntityDataTypes.BOOLEAN, false));
    data.add(new EntityData(12, EntityDataTypes.INT, 0));
    data.add(new EntityData(13, EntityDataTypes.INT, 0));
    data.add(new EntityData(14, EntityDataTypes.OPTIONAL_BLOCK_POSITION, Optional.empty()));
    return data;
  }
}
