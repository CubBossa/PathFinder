package de.cubbossa.pathfinder.editmode.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class DataUtils {

  private static final Pattern PTN_ITEMSTACK =
      Pattern.compile("\\{id:\"([a-z:_])\",Count:([0-9]+)b,tag:(\\{.*})}");

  public String serializeItemStack(ItemStack stack) {
    return new NBTItem(stack).toString();
  }

  public ItemStack deserializeItemStack(String input) {
    Matcher matcher = PTN_ITEMSTACK.matcher(input);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Input string is not a valid item stack: " + input);
    }
    NamespacedKey typeKey = NamespacedKey.fromString(matcher.group(1));
    if (typeKey == null) {
      throw new IllegalArgumentException("Invalid material: " + matcher.group(1));
    }
    Material type = Registry.MATERIAL.get(typeKey);
    if (type == null) {
      throw new IllegalArgumentException("Invalid material: " + matcher.group(1));
    }
    int amount = Integer.parseInt(matcher.group(2));
    NBTCompound nbt = new NBTContainer(matcher.group(3));

    ItemStack stack = new ItemStack(type, amount);
    NBTItem item = new NBTItem(stack);
    item.mergeCompound(nbt);
    return item.getItem();
  }
}
