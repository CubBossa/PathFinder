package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.event.PathCancelledEvent;
import de.cubbossa.pathapi.event.PathStartEvent;
import de.cubbossa.pathapi.event.PathStoppedEvent;
import de.cubbossa.pathapi.event.PathTargetReachedEvent;
import de.cubbossa.pathapi.event.PlayerDiscoverLocationEvent;
import de.cubbossa.pathapi.event.PlayerDiscoverProgressEvent;
import de.cubbossa.pathapi.event.PlayerForgetLocationEvent;
import de.cubbossa.pathapi.group.DiscoverProgressModifier;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.PathFinderConfigImpl;
import de.cubbossa.pathfinder.command.CmdTagResolver;
import de.cubbossa.pathfinder.command.CommandPlaceholderProcessor;
import de.cubbossa.pathfinder.command.CommandPlaceholderProcessorImpl;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.event.PathCancelledEvent;
import de.cubbossa.pathfinder.event.PathStartEvent;
import de.cubbossa.pathfinder.event.PathStoppedEvent;
import de.cubbossa.pathfinder.event.PathTargetReachedEvent;
import de.cubbossa.pathfinder.event.PlayerDiscoverLocationEvent;
import de.cubbossa.pathfinder.event.PlayerDiscoverProgressEvent;
import de.cubbossa.pathfinder.event.PlayerForgetLocationEvent;
import de.cubbossa.pathfinder.group.DiscoverProgressModifier;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.Translator;
import java.util.List;
import de.cubbossa.tinytranslations.util.FormattableBuilder;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitEffects {

  @Setter
  @Getter
  private MiniMessage miniMessage;
  private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

  public BukkitEffects(EventDispatcher<Player> dispatcher, PathFinderConfigImpl.EffectsConfigImpl config) {

    miniMessage = MiniMessage.miniMessage();

    dispatcher.listen(PathStartEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStart, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PathTargetReachedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathTargetReach, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PathCancelledEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathCancel, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PathStoppedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStop, FormattableBuilder.builder()
          .insertObject("player", e.getPath().getTargetViewer())
          .toResolver()
      );
    });

    dispatcher.listen(PlayerDiscoverLocationEvent.class, e -> {
      runCommands(e.getPlayer(), config.onDiscover, FormattableBuilder.builder()
          .insertObject("player", e.getPlayer())
          .insertObject("discoverable", e.getPlayer().getDisplayName())
          .insertObject("group", e.getGroup())
          .toResolver()
      );
    });

    dispatcher.listen(PlayerDiscoverProgressEvent.class, e -> {

      DiscoverableModifier discoverableModifier = e.getFoundGroup().<DiscoverableModifier>getModifier(DiscoverableModifier.KEY)
          .orElseThrow();
      DiscoverProgressModifier discoverProgressModifier = e.getProgressObserverGroup().<DiscoverProgressModifier>getModifier(DiscoverProgressModifier.key)
          .orElseThrow();

      runCommands(e.getPlayer(), config.onDiscoverProgress, //"player", "discoverable", "group", "name", "percent", "ratio", "count-found", "count-all"
          Placeholder.component("player", e.getPlayer().displayName),
          Placeholder.component("discoverable", discoverableModifier.getDisplayName()),
          Placeholder.parsed("group", e.getProgressObserverGroup().getKey().toString()),
          Placeholder.component("name", discoverProgressModifier.getDisplayName()),
          Formatter.number("percentage", discoverProgressModifier.calculateProgress(e.getPlayer().uniqueId).join() * 100)
      );
    });

    dispatcher.listen(PlayerForgetLocationEvent.class, e -> {
      runCommands(e.getPlayer(), config.onForget,
          Placeholder.component("player", e.getPlayer().displayName),
          Placeholder.component("discoverable", e.getModifier().getDisplayName()),
          Placeholder.parsed("group", e.getGroup().getKey().toString())
      );
    });
  }

  private String prepareCmd(String cmd, PathPlayer<Player> player, TagResolver... msgResolvers) {
    // TODO check if placeholderapi installed and if use placeholderapi placeholders
    return processor.process(cmd,
        CmdTagResolver.tag("player", strings -> {
          String arg1 = strings.poll();
          if (arg1 == null) {
            return player.name;
          }
          return switch (arg1) {
            case "id" -> player.uniqueId.toString();
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
            default -> player.name;
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

          Translator translator = AbstractPathFinder.getInstance().getTranslations();

          Component cmp = translator.translate(new Message(messageKey, translator).formatted(
              msgResolvers
          ), AbstractPathFinder.getInstance().getAudiences().player(player.uniqueId));

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
        .peek(System.out::println)
        .forEach(r -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), r));
  }
}
