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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitEffects {

  private final CommandPlaceholderProcessor processor;
  private final GsonComponentSerializer gsonComponentSerializer;

  public BukkitEffects(EventDispatcher<Player> dispatcher, PathFinderConf.EffectsConf config) {

    processor = new CommonCmdPlaceholderProcessor();
    gsonComponentSerializer = GsonComponentSerializer.gson();

    dispatcher.listen(PathTargetReachedEvent.class, e -> {
      e.getPath().getTargetViewer().sendMessage(Messages.TARGET_FOUND);
      runCommands(e.getPath().getTargetViewer(), config.onPathTargetReach);
    });

    dispatcher.listen(PathCancelledEvent.class, e -> {
      e.getPath().getTargetViewer().sendMessage(Messages.CMD_CANCEL);
      runCommands(e.getPath().getTargetViewer(), config.onPathCancel);
    });

    dispatcher.listen(PathStoppedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStop);
    });

    dispatcher.listen(PlayerDiscoverLocationEvent.class, e -> {
      e.getPlayer().sendMessage(e.getModifier().getDisplayName());
      runCommands(e.getPlayer(), config.onDiscover);
    });

    dispatcher.listen(PlayerForgetLocationEvent.class, e -> {
      e.getPlayer().sendMessage(Component.text("Forget: ").append(e.getModifier().getDisplayName()));
    });
  }

  private String prepareCmd(String cmd, PathPlayer<Player> player) {
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
            case "loc" -> {
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
        CmdTagResolver.tag("msg", strings -> {
          if (strings.size() == 0) {
            throw new IllegalStateException("Cannot parse message without message key.");
          }
          Translator translator = CommonPathFinder.getInstance().getTranslations();
          Component cmp = translator.translate(new Message(strings.poll(), translator).formatted(
              // TODO resolve player, discoverable, group, ...
          ), CommonPathFinder.getInstance().getAudiences().player(player.getUniqueId()));

          return gsonComponentSerializer.serialize(cmp);
        })
    );
  }

  private void runCommands(PathPlayer<Player> player, List<String> commands) {
    if (commands == null) {
      return;
    }
    commands.stream()
        .map(s -> this.prepareCmd(s, player))
        .forEach(r -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), r));
  }

}
