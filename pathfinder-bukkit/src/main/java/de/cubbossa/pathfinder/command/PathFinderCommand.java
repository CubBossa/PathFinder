package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.dump.DumpWriterProvider;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.*;
import de.cubbossa.pathfinder.command.impl.*;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.CommandTree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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

    then(Arguments.literal("createdump")
        .withPermission(PathPerms.PERM_CMD_PF_DUMP)
        .executes((sender, args) -> {
          try {
            File dir = PathFinderPlugin.getInstance().getDataFolder();
            String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File dump = new File(dir, "dump_" + date + ".json");
            dir.mkdirs();
            dump.createNewFile();
            DumpWriterProvider.get().save(dump);

            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_DUMP_SUCCESS);
          } catch (IOException t) {
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_DUMP_FAIL);
            pathFinder.getLogger().log(Level.SEVERE, "Could not create dump file.", t);
          }
        })
    );

    then(Arguments.literal("info")
        .withPermission(PathPerms.PERM_CMD_PF_INFO)
        .executes((commandSender, objects) -> {
          BukkitUtils.wrap(commandSender).sendMessage(Messages.INFO.formatted(
              Placeholder.unparsed("version", PathFinderProvider.get().getVersion())
          ));
        }));

    then(Arguments.literal("modules")
        .withPermission(PathPerms.PERM_CMD_PF_MODULES)
        .executes((commandSender, args) -> {
          List<String> list =
              PathFinderProvider.get().getExtensionRegistry().getExtensions().stream()
                  .map(PathFinderExtension::getKey)
                  .map(NamespacedKey::toString).toList();

          BukkitUtils.wrap(commandSender).sendMessage(Messages.MODULES.insertList("modules", list
              .stream().map(Component::text).toList()));
        }));

    then(Arguments.literal("editmode")
        .executesPlayer((player, args) -> {
          NodeHandler.getInstance()
              .toggleNodeGroupEditor(BukkitUtils.wrap(player), CommonPathFinder.globalGroupKey());
        })
        .then(Arguments.nodeGroupArgument("group")
            .executesPlayer((player, args) -> {
              NodeHandler.getInstance().toggleNodeGroupEditor(BukkitUtils.wrap(player),
                  ((SimpleNodeGroup) args.getUnchecked(0)).getKey());
            })));

    then(Arguments.literal("help")
        .withPermission(PathPerms.PERM_CMD_PF_HELP)
        .executes((commandSender, objects) -> {
          BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_HELP);
        }));

    then(Arguments.literal("reload")
        .withPermission(PathPerms.PERM_CMD_PF_RELOAD)

        .executes((sender, objects) -> {
          long now = System.currentTimeMillis();

          CompletableFuture.runAsync(() -> {
            CommonPathFinder pf = BukkitPathFinder.getInstance();
            pf.loadConfig();
            pf.reloadLocales(pathFinder.getConfiguration());

          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(Placeholder.component("error", Component.text(throwable.getMessage()
                  .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
              );
              PathFinderProvider.get().getLogger()
                  .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
            } else {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS
                  .insertNumber("ms", System.currentTimeMillis() - now));
            }
          });
        })

        .then(Arguments.literal("language")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                CommonPathFinder pf = BukkitPathFinder.getInstance();
                pf.reloadLocales(pathFinder.getConfiguration());

              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(
                      Placeholder.component("error", Component.text(throwable.getMessage()
                          .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", "")))
                  ));
                  PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_LANG
                      .insertNumber("ms", System.currentTimeMillis() - now));
                }
              });
            })
        )

        .then(Arguments.literal("config")
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
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_CFG
                      .insertNumber("ms", System.currentTimeMillis() - now));
                }
              });
            })
        )
    );

    then(Arguments.literal("forcefind")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFIND)
        .then(Arguments.pathPlayers("player")
            .withGeneratedHelp()
            .then(Arguments.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                  for (PathPlayer<Player> player : args.<Collection<PathPlayer<Player>>>getUnchecked(0)) {
                    onForceFind(commandSender, player, args.getUnchecked(1));
                  }
                }))));
    then(Arguments.literal("forceforget")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFORGET)
        .then(Arguments.pathPlayers("player")
            .withGeneratedHelp()
            .then(Arguments.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                  for (PathPlayer<Player> player : args.<Collection<PathPlayer<Player>>>getUnchecked(0)) {
                    onForceForget(BukkitUtils.wrap(commandSender), player, args.getUnchecked(1));
                  }
                }))));
    then(Arguments.literal("worldid")
        .executesPlayer((sender, args) -> {
          BukkitUtils.wrap(sender).sendMessage(Component.text(sender.getWorld().getUID().toString())
              .clickEvent(ClickEvent.copyToClipboard(sender.getWorld().getUID().toString())));
        }));
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

          BukkitUtils.wrap(sender).sendMessage(Messages.CMD_FORCE_FIND.formatted(
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

          sender.sendMessage(Messages.CMD_FORCE_FORGET.formatted(
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
