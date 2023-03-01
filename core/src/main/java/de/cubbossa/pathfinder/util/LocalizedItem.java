package de.cubbossa.pathfinder.util;

import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LocalizedItem {

  private static final LegacyComponentSerializer serializer =
      LegacyComponentSerializer.builder().build();
  private final ItemStack stack;
  private final Message name;
  private final Message lore;
  private TagResolver[] nameResolver = new TagResolver[0];
  private TagResolver[] loreResolver = new TagResolver[0];

  public LocalizedItem(Material type, Message name, Message lore) {
    this(new ItemStack(type), name, lore);
  }

  public LocalizedItem(ItemStack stack, Message name, Message lore) {
    this.stack = stack;
    this.name = name;
    this.lore = lore;
  }

  public LocalizedItem(ItemStack stack, Message name, TagResolver[] nameResolver, Message lore,
                       TagResolver[] loreResolver) {
    this(stack, name, lore);
    this.nameResolver = nameResolver;
    this.loreResolver = loreResolver;
  }

  public ItemStack createItem(Player player) {

    if (stack.getType() == Material.AIR) {
      return stack.clone();
    }
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(serializer.serialize(
        TranslationHandler.getInstance().translateLine(name, player, nameResolver)));
    meta.setLore(
        TranslationHandler.getInstance().translateLines(lore, player, loreResolver).stream()
            .map(serializer::serialize).toList());
    stack.setItemMeta(meta);
    return stack;
  }

  public static class Builder {

    private final ItemStack stack;
    private final List<TagResolver> nameResolvers = new ArrayList<>();
    private final List<TagResolver> loreResolvers = new ArrayList<>();
    private Message name = null;
    private Message lore = null;

    public Builder(ItemStack stack) {
      this.stack = stack;
    }

    public Builder withName(Message message) {
      this.name = message;
      if (message instanceof FormattedMessage formatted) {
        this.nameResolvers.addAll(List.of(formatted.getResolvers()));
      }
      return this;
    }

    public Builder withLore(Message message) {
      this.lore = message;
      if (message instanceof FormattedMessage formatted) {
        this.loreResolvers.addAll(List.of(formatted.getResolvers()));
      }
      return this;
    }

    public Builder withNameResolver(TagResolver resolver) {
      this.nameResolvers.add(resolver);
      return this;
    }

    public Builder withLoreResolver(TagResolver resolver) {
      this.loreResolvers.add(resolver);
      return this;
    }

    public LocalizedItem build() {
      return new LocalizedItem(stack, name, nameResolvers.toArray(TagResolver[]::new), lore,
          loreResolvers.toArray(TagResolver[]::new));
    }

    public ItemStack createItem(Player player) {
      return build().createItem(player);
    }
  }
}
