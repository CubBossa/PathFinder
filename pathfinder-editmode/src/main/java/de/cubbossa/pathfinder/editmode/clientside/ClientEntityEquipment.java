package de.cubbossa.pathfinder.editmode.clientside;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ClientEntityEquipment implements EntityEquipment {

  private final ClientLivingEntity holder;
  private final Map<EquipmentSlot, ItemStack> map = new HashMap<>();

  public ClientEntityEquipment(ClientLivingEntity holder) {
    this.holder = holder;
  }

  @Override
  public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
    map.put(slot, item);
    holder.equipmentChanged = true;
  }

  @Override
  public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item, boolean silent) {
    setItem(slot, item);
  }

  @NotNull
  @Override
  public ItemStack getItem(@NotNull EquipmentSlot slot) {
    return map.get(slot);
  }

  @NotNull
  @Override
  public ItemStack getItemInMainHand() {
    return map.get(EquipmentSlot.HAND);
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {
    setItem(EquipmentSlot.HAND, item);
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item, boolean silent) {
    setItemInMainHand(item);
  }

  @NotNull
  @Override
  public ItemStack getItemInOffHand() {
    return map.get(EquipmentSlot.OFF_HAND);
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {
    setItem(EquipmentSlot.OFF_HAND, item);
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item, boolean silent) {
    setItemInOffHand(item);
  }

  @NotNull
  @Override
  public ItemStack getItemInHand() {
    return getItemInMainHand();
  }

  @Override
  public void setItemInHand(@Nullable ItemStack stack) {
    setItemInMainHand(stack);
  }

  @Nullable
  @Override
  public ItemStack getHelmet() {
    return map.get(EquipmentSlot.HEAD);
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {
    setItem(EquipmentSlot.HEAD, helmet);
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet, boolean silent) {
    setHelmet(helmet);
  }

  @Nullable
  @Override
  public ItemStack getChestplate() {
    return map.get(EquipmentSlot.CHEST);
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {
    setItem(EquipmentSlot.CHEST, chestplate);
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate, boolean silent) {
    setChestplate(chestplate);
  }

  @Nullable
  @Override
  public ItemStack getLeggings() {
    return map.get(EquipmentSlot.LEGS);
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {
    setItem(EquipmentSlot.LEGS, leggings);
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings, boolean silent) {
    setLeggings(leggings);
  }

  @Nullable
  @Override
  public ItemStack getBoots() {
    return map.get(EquipmentSlot.FEET);
  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {
    setItem(EquipmentSlot.FEET, boots);
  }

  @Override
  public void setBoots(@Nullable ItemStack boots, boolean silent) {
    setBoots(boots);
  }

  @NotNull
  @Override
  public ItemStack[] getArmorContents() {
    return new ItemStack[]{map.get(EquipmentSlot.FEET), map.get(EquipmentSlot.LEGS), map.get(EquipmentSlot.CHEST), map.get(EquipmentSlot.HEAD)};
  }

  @Override
  public void setArmorContents(@NotNull ItemStack[] items) {
    setItem(EquipmentSlot.FEET, items[0]);
    setItem(EquipmentSlot.CHEST, items[1]);
    setItem(EquipmentSlot.LEGS, items[2]);
    setItem(EquipmentSlot.FEET, items[3]);
  }

  @Override
  public void clear() {
    map.clear();
    holder.equipmentChanged = true;
  }

  @Override
  public float getItemInHandDropChance() {
    return 0;
  }

  @Override
  public void setItemInHandDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public float getItemInMainHandDropChance() {
    return 0;
  }

  @Override
  public void setItemInMainHandDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public float getItemInOffHandDropChance() {
    return 0;
  }

  @Override
  public void setItemInOffHandDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public float getHelmetDropChance() {
    return 0;
  }

  @Override
  public void setHelmetDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public float getChestplateDropChance() {
    return 0;
  }

  @Override
  public void setChestplateDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public float getLeggingsDropChance() {
    return 0;
  }

  @Override
  public void setLeggingsDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public float getBootsDropChance() {
    return 0;
  }

  @Override
  public void setBootsDropChance(float chance) {
    throw new ClientEntityMethodNotSupportedException();
  }
}
