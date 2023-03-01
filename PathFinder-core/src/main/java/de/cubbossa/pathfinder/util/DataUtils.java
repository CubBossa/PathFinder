package de.cubbossa.pathfinder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

@UtilityClass
public class DataUtils {

  private static final Pattern PTN_ITEMSTACK =
      Pattern.compile("\\{id:\"([a-z:_])\",Count:([0-9]+)b,tag:(\\{.*})}");

  public String serializeParticle(ParticleBuilder particle) {
    JsonObject obj = new JsonObject();
    obj.addProperty("type", "NONE");
    return obj.toString();
  }

  public ParticleBuilder deserializeParticle(String input) {

    JsonObject object = JsonParser.parseString(input).getAsJsonObject();
    String type = object.get("type").getAsString();
    int amount = object.get("amount").getAsInt();
    JsonArray offsetArray = object.get("offset").getAsJsonArray();
    Vector offset = new Vector(
        offsetArray.get(0).getAsDouble(),
        offsetArray.get(1).getAsDouble(),
        offsetArray.get(2).getAsDouble());
    float speed = object.get("speed").getAsFloat();
    JsonObject data = object.get("data").getAsJsonObject();


    return new ParticleBuilder(ParticleEffect.valueOf(type.toUpperCase()))
        .setAmount(amount)
        .setOffset(offset)
        .setSpeed(speed);
  }

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
