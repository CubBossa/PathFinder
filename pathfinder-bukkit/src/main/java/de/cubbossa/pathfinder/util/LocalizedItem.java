package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.translations.ComponentSplit;
import de.cubbossa.translations.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LocalizedItem {

  private static final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
  private final ItemStack stack;
  private final Message name;
  private final Message lore;

  public LocalizedItem(Material type, Message name, Message lore) {
    this(new ItemStack(type), name, lore);
  }

  public LocalizedItem(ItemStack stack, Message name, Message lore) {
    this.stack = stack;
    this.name = name;
    this.lore = lore;
  }

  public ItemStack createItem(Player player) {

    if (stack.getType() == Material.AIR) {
      return stack.clone();
    }
    AbstractPathFinder pf = BukkitPathFinder.getInstance();

    ItemMeta meta = stack.getItemMeta();
    Audience audience = pf.getAudiences().player(player.getUniqueId());
    meta.setDisplayName(serializer.serialize(name.getTranslator().translate(name, audience)));
    meta.setLore(ComponentSplit.split(pf.getTranslations().translate(lore, audience), "\n").stream()
        .map(serializer::serialize).toList());
    stack.setItemMeta(meta);
    return stack;
  }

  public static class Builder {

    private final ItemStack stack;
    private Message name = null;
    private Message lore = null;

    public Builder(ItemStack stack) {
      this.stack = stack;
    }

    public Builder withName(Message message) {
      this.name = message;
      return this;
    }

    public Builder withLore(Message message) {
      this.lore = message;
      return this;
    }

    public LocalizedItem build() {
      return new LocalizedItem(stack, name, lore);
    }

    public ItemStack createItem(Player player) {
      return build().createItem(player);
    }
  }
}
