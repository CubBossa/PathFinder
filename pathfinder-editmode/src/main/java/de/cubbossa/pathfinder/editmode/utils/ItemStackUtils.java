package de.cubbossa.pathfinder.editmode.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.cubbossa.translations.Message;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class ItemStackUtils {

  public static String HEAD_URL_LETTER_CHECK_MARK =
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkyZTMxZmZiNTljOTBhYjA4ZmM5ZGMxZmUyNjgwMjAzNWEzYTQ3YzQyZmVlNjM0MjNiY2RiNDI2MmVjYjliNiJ9fX0=";
  public static String HEAD_URL_LETTER_EXCLAMATION =
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYyNDVmYjM5N2I3YzJiM2EzNmUyYTI0ZDQ5NmJlMjU4ZjFjZGY0MTA1NGY5OWU5YzY1ZTFhNjczYWRkN2I0In19fQ==";

  public static String HEAD_URL_ORANGE =
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5YWRkM2U1OTM2YTM4MmE4ZjdmZGMzN2ZkNmZhOTY2NTNkNTEwNGViY2FkYjBkNGY3ZTlkNGE2ZWZjNDU0In19fQ==";
  public static String HEAD_URL_BLUE =
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGMzNzRhY2VhNzhlZmJlZmE3OThiZTFiMjdlOTcxNGMzNjQxMWUyMDJlZWNkMzdiOGNmY2ZkMjQ5YTg2MmUifX19";
  public static String HEAD_URL_GREEN =
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5YjI3ZmNjZDgwOTIxYmQyNjNjOTFkYzUxMWQwOWU5YTc0NjU1NWU2YzljYWQ1MmU4NTYyZWQwMTgyYTJmIn19fQ==";

  public GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.builder().build();
  public LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
      .character('§')
      .hexColors()
      .useUnusualXRepeatedCharacterHexFormat()
      .hexCharacter('x')
      .build();

  public void giveOrDrop(Player player, ItemStack itemStack) {
    giveOrDrop(player, itemStack, player.getLocation());
  }

  public void giveOrDrop(Player player, @Nullable ItemStack item, Location location) {

    if (item == null || item.getType() == Material.AIR) {
      return;
    }
    Map<Integer, ItemStack> leftoverItems = player.getInventory().addItem(item.clone());
    if (leftoverItems.isEmpty()) {
      return;
    }
    leftoverItems.forEach((index, item2) -> location.getWorld().dropItemNaturally(location, item2));
  }

  public ItemStack addLore(ItemStack itemStack, List<Component> lore) {
    NBTItem item = new NBTItem(itemStack);
    NBTCompound display = item.getCompound("display");
    if (display == null) {
      display = item.addCompound("display");
    }
    List<String> presentLore = display.getStringList("Lore");
    presentLore.addAll(lore.stream().map(component -> {
      return component.decoration(TextDecoration.ITALIC,
          component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET ?
              TextDecoration.State.FALSE : component.decoration(TextDecoration.ITALIC));
    }).map(component -> GSON_SERIALIZER.serialize(component)).collect(Collectors.toList()));
    return item.getItem();
  }

  public ItemStack setDisplayName(ItemStack stack, ComponentLike name) {
    Component n = name.asComponent();
    NBTItem item = new NBTItem(stack);
    NBTCompound display = item.getCompound("display");
    if (display == null) {
      display = item.addCompound("display");
    }
    TextDecoration.State decoration = n.decoration(TextDecoration.ITALIC);
    display.setString("Name", GSON_SERIALIZER.serialize(n.decoration(TextDecoration.ITALIC,
        decoration.equals(TextDecoration.State.NOT_SET) ? TextDecoration.State.FALSE
            : decoration)));
    return item.getItem();
  }

  public ItemStack setLore(ItemStack itemStack, List<? extends ComponentLike> lore) {
    NBTItem item = new NBTItem(itemStack);
    NBTCompound display = item.getCompound("display");
    if (display == null) {
      display = item.addCompound("display");
    }
    List<String> presentLore = display.getStringList("Lore");
    presentLore.clear();
    presentLore.addAll(lore.stream().map(ComponentLike::asComponent).map(component -> {
      return component.decoration(TextDecoration.ITALIC,
          component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET ?
              TextDecoration.State.FALSE : component.decoration(TextDecoration.ITALIC));
    }).map(component -> GSON_SERIALIZER.serialize(component)).collect(Collectors.toList()));
    return item.getItem();
  }

  public ItemStack setCustomModelData(ItemStack itemStack, int customModelData) {
    ItemMeta meta = itemStack.getItemMeta();
    if (meta == null) {
      meta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    }
    meta.setCustomModelData(customModelData);
    itemStack.setItemMeta(meta);
    return itemStack;
  }

  public ItemStack createItemStack(Material material, ComponentLike name) {
    ItemStack stack = new ItemStack(material);
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      meta = Bukkit.getItemFactory().getItemMeta(material);
    }
    if (meta == null) {
      throw new IllegalStateException("Could not create itemstack.");
    }
    meta.setDisplayName(SERIALIZER.serialize(name.asComponent()));
    stack.setItemMeta(meta);
    return stack;
  }

  public ItemStack createItemStack(Material material, int customModelData) {
    return ItemStackUtils.setCustomModelData(new ItemStack(material), customModelData);
  }

  public ItemStack createCustomHead(String url) {
    return createCustomHead(new ItemStack(Material.PLAYER_HEAD, 1), url);
  }

  public ItemStack createCustomHead(ItemStack itemStack, String url) {
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta instanceof SkullMeta meta) {
      GameProfile profile = new GameProfile(UUID.randomUUID(), null);
      profile.getProperties().put("textures", new Property("textures", url));

      try {
        Field profileField = meta.getClass().getDeclaredField("profile");
        profileField.setAccessible(true);
        profileField.set(meta, profile);

      } catch (IllegalArgumentException | NoSuchFieldException | SecurityException |
               IllegalAccessException error) {
        error.printStackTrace();
      }
      itemStack.setItemMeta(meta);
    } else {
      throw new UnsupportedOperationException(
          "Trying to add a skull texture to a non-playerhead item");
    }
    return itemStack;
  }

  public ItemStack createInfoItem(Message name, Message lore) {
    ItemStack stack = new ItemStack(Material.PAPER, 1);
    stack = setNameAndLore(stack, name, lore);
    stack = setCustomModelData(stack, 7121000);
    return stack;
  }

  public ItemStack setFlags(ItemStack stack) {
    ItemMeta meta = stack.getItemMeta();
    meta.addItemFlags(ItemFlag.values());
    stack.setItemMeta(meta);
    return stack;
  }

  public ItemStack setNameAndLore(ItemStack itemStack, ComponentLike name,
                                  List<? extends ComponentLike> lore) {
    itemStack = setDisplayName(itemStack, name);
    itemStack = setLore(itemStack, lore);
    return itemStack;
  }

  public ItemStack setNameAndLore(ItemStack itemStack, Message name, Message lore) {
    itemStack = setDisplayName(itemStack, name);
    itemStack = setLore(itemStack, List.of(lore.asComponent()));
    return itemStack;
  }

  public ItemStack setGlow(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      meta = Bukkit.getItemFactory().getItemMeta(item.getType());
    }
    if (meta != null) {
      meta.addEnchant(Enchantment.FLAME, 1, true);
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(meta);
    }
    return item;
  }
}
