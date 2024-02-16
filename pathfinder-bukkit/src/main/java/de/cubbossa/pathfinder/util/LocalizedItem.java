package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TinyTranslations;
import de.cubbossa.tinytranslations.util.ComponentSplit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
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
    CommonPathFinder pf = BukkitPathFinder.getInstance();

    ItemMeta meta = stack.getItemMeta();

    Audience audience = pf.getAudiences().player(player.getUniqueId());


    meta.setDisplayName(serializer.serialize(render(name, audience)));
    meta.setLore(ComponentSplit.split(render(lore, audience), "\n").stream()
        .map(serializer::serialize).toList());
    stack.setItemMeta(meta);
    return stack;
  }

  private static Component render(ComponentLike component, Audience audience) {
    return GlobalTranslator.renderer().render(component.asComponent(), audience.getOrDefault(Identity.LOCALE, TinyTranslations.DEFAULT_LOCALE));
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
