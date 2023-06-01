package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.impl.*;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.module.AbstractDiscoverHandler;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.translations.MessageBundle;
import dev.jorel.commandapi.CommandTree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * The basic command of this plugin, which handles things like reload, export, import, etc.
 */
public class PathFinderCommand extends CommandTree {

  private final PathFinder pathFinder;

  /**
   * The basic command of this plugin, which handles things like reload, export, import, etc.
   */
  public PathFinderCommand(PathFinder pathFinder) {
    super("pathfinder");
    this.pathFinder = pathFinder;

    withAliases("pf");

    then(new NodesCmd(pathFinder));
    NodeType<?> type = pathFinder.getNodeTypeRegistry().getType(CommonPathFinder.pathfinder("waypoint"));
    then(new CreateNodeCmd(pathFinder, () -> type));
    then(new DeleteNodesCmd(pathFinder));
    then(new ListNodesCmd(pathFinder));
    then(new NodesCmd(pathFinder));

    then(new CreateGroupCmd(pathFinder));
    then(new DeleteGroupCmd(pathFinder));
    then(new ListGroupsCmd(pathFinder));
    then(new GroupCmd(pathFinder));

    then(new CreateVisualizerCmd(pathFinder));
    then(new DeleteVisualizerCmd(pathFinder));
    then(new ImportVisualizerCmd(pathFinder));
    then(new ListVisualizersCmd(pathFinder));
    then(new VisualizerCmd(pathFinder));

    withRequirement(sender ->
        sender.hasPermission(PathPerms.PERM_CMD_PF_HELP)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_INFO)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_IMPORT_VIS)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_EXPORT)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_RELOAD)
    );

    executes((sender, args) -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.HELP.formatted(
          Placeholder.parsed("version", PathFinderProvider.get().getVersion())
      ));
    });

    then(CustomArgs.literal("info")
        .withPermission(PathPerms.PERM_CMD_PF_INFO)
        .executes((commandSender, objects) -> {
          BukkitUtils.wrap(commandSender).sendMessage(Messages.INFO.formatted(
              Placeholder.unparsed("version", PathFinderProvider.get().getVersion())
          ));
        }));

    then(CustomArgs.literal("modules")
        .withPermission(PathPerms.PERM_CMD_PF_MODULES)
        .executes((commandSender, args) -> {
          List<String> list =
              PathFinderProvider.get().getExtensionRegistry().getExtensions().stream()
                  .map(PathFinderExtension::getKey)
                  .map(NamespacedKey::toString).toList();

          BukkitUtils.wrap(commandSender).sendMessage(Messages.MODULES.formatted(
              Messages.formatter().list("modules", list, Component::text)
          ));
        }));

    then(CustomArgs.literal("editmode")
        .executesPlayer((player, args) -> {
          NodeHandler.getInstance()
              .toggleNodeGroupEditor(BukkitUtils.wrap(player), CommonPathFinder.globalGroupKey());
        })
        .then(CustomArgs.nodeGroupArgument("group")
            .executesPlayer((player, args) -> {
              NodeHandler.getInstance().toggleNodeGroupEditor(BukkitUtils.wrap(player),
                  ((SimpleNodeGroup) args.getUnchecked(0)).getKey());
            })));

    then(CustomArgs.literal("help")
        .withPermission(PathPerms.PERM_CMD_PF_HELP)
        .executes((commandSender, objects) -> {
          BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_HELP);
        }));

    then(CustomArgs.literal("reload")
        .withPermission(PathPerms.PERM_CMD_PF_RELOAD)

        .executes((sender, objects) -> {
          long now = System.currentTimeMillis();

          CompletableFuture.runAsync(() -> {
            CommonPathFinder pf = BukkitPathFinder.getInstance();
            MessageBundle translations = pf.getTranslations();

            translations.clearCache();
            translations.writeLocale(Locale.ENGLISH); // TODO
            translations.loadLocale(Locale.ENGLISH);
          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(Placeholder.component("error", Component.text(throwable.getMessage()
                  .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
              );
              PathFinderProvider.get().getLogger()
                  .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
            } else {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS.formatted(
                  Messages.formatter().number("ms", System.currentTimeMillis() - now)
              ));
            }
          });
        })

        .then(CustomArgs.literal("language")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                CommonPathFinder pf = BukkitPathFinder.getInstance();
                MessageBundle translations = pf.getTranslations();

                translations.clearCache();
                translations.writeLocale(Locale.ENGLISH); // TODO
                translations.loadLocale(Locale.ENGLISH);
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(
                      Placeholder.component("error", Component.text(throwable.getMessage()
                          .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", "")))
                  ));
                  PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_LANG.formatted(
                      Messages.formatter().number("ms", System.currentTimeMillis() - now)
                  ));
                }
              });
            })
        )

        .then(CustomArgs.literal("effects")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                try {
                  // TODO
                } catch (Throwable t) {
                  throw new RuntimeException(t);
                }
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(TagResolver.builder()
                      .resolver(Placeholder.component("error", Component.text(throwable.getMessage()
                          .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                      .build()));
                  PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_FX.formatted(
                      Messages.formatter().number("ms", System.currentTimeMillis() - now)
                  ));
                }
              });
            })
        )
        .then(CustomArgs.literal("config")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                try {
                  // TODO bah
                  ((CommonPathFinder) PathFinderProvider.get()).loadConfig();
                } catch (Throwable t) {
                  throw new RuntimeException(t);
                }
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(TagResolver.builder()
                      .resolver(Placeholder.component("error", Component.text(
                          throwable.getMessage()
                              .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                      .build()));
                  PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading configuration: ",
                          throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_CFG.formatted(
                      Messages.formatter().number("ms", System.currentTimeMillis())
                  ));
                }
              });
            })
        )
    );

    then(CustomArgs.literal("forcefind")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFIND)
        .then(CustomArgs.pathPlayer("player")
            .withGeneratedHelp()
            .then(CustomArgs.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                  onForceFind(commandSender, args.getUnchecked(0), args.getUnchecked(1));
                }))));
    then(CustomArgs.literal("forceforget")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFORGET)
        .then(CustomArgs.pathPlayer("player")
            .withGeneratedHelp()
            .then(CustomArgs.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                  onForceForget(BukkitUtils.wrap(commandSender), args.getUnchecked(0), args.getUnchecked(1));
                }))));
  }

  private void onForceFind(CommandSender sender, PathPlayer<Player> target, NamespacedKey discoverable) {
    pathFinder.getStorage().loadGroup(discoverable)
        .thenApply(Optional::orElseThrow)
        .thenAccept(group -> {
          Optional<DiscoverableModifier> mod = group.getModifier(DiscoverableModifier.KEY);
          if (mod.isEmpty()) {
            return;
          }

          AbstractDiscoverHandler.<Player>getInstance().discover(target, group, LocalDateTime.now());

          BukkitUtils.wrap(sender).sendMessage(Messages.CMD_RM_FORCE_FIND.formatted(
              Placeholder.component("name", target.getDisplayName()),
              Placeholder.component("discovery", mod.get().getDisplayName()))
          );
        });
  }

  private void onForceForget(PathPlayer<CommandSender> sender, PathPlayer<Player> target, NamespacedKey discoverable) {
    pathFinder.getStorage().loadGroup(discoverable)
        .thenApply(Optional::orElseThrow)
        .thenAccept(group -> {
          Optional<DiscoverableModifier> mod = group.getModifier(DiscoverableModifier.KEY);
          if (mod.isEmpty()) {
            return;
          }

          AbstractDiscoverHandler.<Player>getInstance().forget(target, group);

          sender.sendMessage(Messages.CMD_RM_FORCE_FORGET.formatted(
              Placeholder.unparsed("name", target.getName()),
              Placeholder.component("name", target.getDisplayName()),
              Placeholder.component("discovery", mod.get().getDisplayName()))
          );
        })
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }
}
