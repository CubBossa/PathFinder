package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderConf;
import de.cubbossa.pathfinder.command.CmdTagResolver;
import de.cubbossa.pathfinder.command.CommandPlaceholderProcessor;
import de.cubbossa.pathfinder.command.CommonCmdPlaceholderProcessor;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.Translator;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitEffects {

  private final CommandPlaceholderProcessor processor;

  @Setter
  @Getter
  private MiniMessage miniMessage;
  private final GsonComponentSerializer gsonComponentSerializer;

  public BukkitEffects(EventDispatcher<Player> dispatcher, PathFinderConf.EffectsConf config) {

    processor = new CommonCmdPlaceholderProcessor();

    miniMessage = MiniMessage.miniMessage();
    gsonComponentSerializer = GsonComponentSerializer.gson();

    dispatcher.listen(PathStartEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStart,
          Placeholder.component("player", e.getPath().getTargetViewer().getDisplayName())
      );
    });

    dispatcher.listen(PathTargetReachedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathTargetReach,
          Placeholder.component("player", e.getPath().getTargetViewer().getDisplayName())
      );
    });

    dispatcher.listen(PathCancelledEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathCancel,
          Placeholder.component("player", e.getPath().getTargetViewer().getDisplayName())
      );
    });

    dispatcher.listen(PathStoppedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStop,
          Placeholder.component("player", e.getPath().getTargetViewer().getDisplayName())
      );
    });

    dispatcher.listen(PlayerDiscoverLocationEvent.class, e -> {
      runCommands(e.getPlayer(), config.onDiscover,
          Placeholder.component("player", e.getPlayer().getDisplayName()),
          Placeholder.component("discoverable", e.getModifier().getDisplayName()),
          Messages.formatter().namespacedKey("group", e.getGroup().getKey())
      );
    });

    dispatcher.listen(PlayerForgetLocationEvent.class, e -> {
      runCommands(e.getPlayer(), config.onForget,
          Placeholder.component("player", e.getPlayer().getDisplayName()),
          Placeholder.component("discoverable", e.getModifier().getDisplayName()),
          Messages.formatter().namespacedKey("group", e.getGroup().getKey())
      );
    });
  }

  private String prepareCmd(String cmd, PathPlayer<Player> player, TagResolver... msgResolvers) {
    // TODO check if placeholderapi installed and if use placeholderapi placeholders
    return processor.process(cmd,
        CmdTagResolver.tag("player", strings -> {
          String arg1 = strings.poll();
          if (arg1 == null) {
            return player.getName();
          }
          return switch (arg1) {
            case "id" -> player.getUniqueId().toString();
            case "world" -> {
              String arg2 = strings.poll();
              if (arg2 != null && arg2.equalsIgnoreCase("id")) {
                yield player.unwrap().getWorld().getUID().toString();
              }
              yield player.unwrap().getWorld().getName();
            }
            case "location" -> {
              String arg2 = strings.poll();
              Location loc = player.unwrap().getLocation();
              if (arg2 == null) {
                yield loc.getX() + " " + loc.getY() + " " + loc.getZ();
              }
              yield switch (arg2) {
                case "world" -> loc.getWorld().getName();
                case "x" -> String.valueOf(loc.getX());
                case "y" -> String.valueOf(loc.getY());
                case "z" -> String.valueOf(loc.getZ());
                case "yaw" -> String.valueOf(loc.getYaw());
                case "pitch" -> String.valueOf(loc.getPitch());
                default -> loc.getX() + " " + loc.getY() + " " + loc.getZ();
              };
            }
            default -> player.getName();
          };
        }),
        CmdTagResolver.tag("translation", strings -> {
          if (strings.size() < 1) {
            throw new IllegalStateException("Cannot parse message without message key.");
          }
          String messageKey = "";
          while (strings.size() > 1) messageKey += "." + strings.poll();
          messageKey = messageKey.substring(1);
          String serializer = strings.poll();

          Translator translator = CommonPathFinder.getInstance().getTranslations();

          Component cmp = translator.translate(new Message(messageKey, translator).formatted(
              msgResolvers
          ), CommonPathFinder.getInstance().getAudiences().player(player.getUniqueId()));

          return (switch (serializer) {
            case "miniMessage", "mini" -> miniMessage;
            default -> gsonComponentSerializer;
          }).serialize(cmp);
        })
    );
  }

  private void runCommands(PathPlayer<Player> player, List<String> commands, TagResolver... msgResolvers) {
    if (commands == null) {
      return;
    }
    commands.stream()
        .map(s -> this.prepareCmd(s, player, msgResolvers))
        .forEach(r -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), r));
  }
}
