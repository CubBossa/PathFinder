package de.cubbossa.pathfinder.messages;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Vector;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public interface MessageFormatter {

  void setNullStyle(Style style);

  void setTextStyle(Style style);

  void setNumberStyle(Style style);

  void setMiniMessage(MiniMessage miniMessage);

  TagResolver throwable(Throwable throwable);

  TagResolver choice(String key, boolean value);

  TagResolver number(String key, Number value);

  TagResolver uuid(String key, UUID value);

  TagResolver namespacedKey(String key, NamespacedKey namespacedKey);

  TagResolver permission(String key, @Nullable String permission);

  TagResolver vector(String key, Vector vector);

  TagResolver particle(String key, Particle particle, Object data);

  <C extends ComponentLike> TagResolver list(String key, Collection<C> entries);

  <C> TagResolver list(String key, Collection<C> entries, Function<C, ComponentLike> renderer);

  TagResolver modifiers(String key, Collection<Modifier> modifiers);
}
