package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.impl.CreateGroupCmd;
import de.cubbossa.pathfinder.command.impl.CreateNodeCmd;
import de.cubbossa.pathfinder.command.impl.CreateVisualizerCmd;
import de.cubbossa.pathfinder.command.impl.DeleteGroupCmd;
import de.cubbossa.pathfinder.command.impl.DeleteNodesCmd;
import de.cubbossa.pathfinder.command.impl.DeleteVisualizerCmd;
import de.cubbossa.pathfinder.command.impl.GroupCmd;
import de.cubbossa.pathfinder.command.impl.ImportVisualizerCmd;
import de.cubbossa.pathfinder.command.impl.ListGroupsCmd;
import de.cubbossa.pathfinder.command.impl.ListNodesCmd;
import de.cubbossa.pathfinder.command.impl.ListVisualizersCmd;
import de.cubbossa.pathfinder.command.impl.NavigateCmd;
import de.cubbossa.pathfinder.command.impl.NodesCmd;
import de.cubbossa.pathfinder.command.impl.VisualizerCmd;
import de.cubbossa.pathfinder.discovery.AbstractDiscoveryModule;
import de.cubbossa.pathfinder.dump.DumpWriterProvider;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.GraphEditorRegistry;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.nodegroup.NodeGroupImpl;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.translations.MessageBundle;
import dev.jorel.commandapi.CommandTree;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    then(new NavigateCmd(pathFinder));

    then(new NodesCmd(pathFinder));
    NodeType<?> type = pathFinder.getNodeTypeRegistry().getType(AbstractPathFinder.pathfinder("waypoint"));
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
          Placeholder.parsed("version", PathFinder.get().getVersion())
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
              Placeholder.unparsed("version", PathFinder.get().getVersion())
          ));
        }));

    then(Arguments.literal("modules")
        .withPermission(PathPerms.PERM_CMD_PF_MODULES)
        .executes((commandSender, args) -> {
          List<String> list =
              PathFinder.get().getExtensionRegistry().getExtensions().stream()
                  .map(PathFinderExtension::getKey)
                  .map(NamespacedKey::toString).toList();

          BukkitUtils.wrap(commandSender).sendMessage(Messages.MODULES.formatted(
              Messages.formatter().list("modules", list, Component::text)
          ));
        }));

    then(Arguments.literal("editmode")
        .executesPlayer((player, args) -> {
          GraphEditorRegistry.getInstance()
              .toggleNodeGroupEditor(BukkitUtils.wrap(player), AbstractPathFinder.globalGroupKey());
        })
        .then(Arguments.nodeGroupArgument("group")
            .executesPlayer((player, args) -> {
              GraphEditorRegistry.getInstance().toggleNodeGroupEditor(BukkitUtils.wrap(player),
                  ((NodeGroupImpl) args.getUnchecked(0)).getKey());
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
            AbstractPathFinder pf = BukkitPathFinder.getInstance();
            pf.getConfigFileLoader().loadConfig();

            MessageBundle translations = pf.getTranslations();

            Locale fallback = pf.getConfiguration().getLanguage().getFallbackLanguage();
            translations.clearCache();
            translations.writeLocale(fallback);
            translations.loadLocale(fallback);
          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(Placeholder.component("error", Component.text(throwable.getMessage()
                  .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
              );
              PathFinder.get().getLogger()
                  .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
            } else {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS.formatted(
                  Messages.formatter().number("ms", System.currentTimeMillis() - now)
              ));
            }
          });
        })

        .then(Arguments.literal("language")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                AbstractPathFinder pf = BukkitPathFinder.getInstance();
                MessageBundle translations = pf.getTranslations();

                Locale fallback = pf.getConfiguration().getLanguage().getFallbackLanguage();
                translations.clearCache();
                translations.writeLocale(fallback);
                translations.loadLocale(fallback);
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(
                      Placeholder.component("error", Component.text(throwable.getMessage()
                          .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", "")))
                  ));
                  PathFinder.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_LANG.formatted(
                      Messages.formatter().number("ms", System.currentTimeMillis() - now)
                  ));
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
                  ((AbstractPathFinder) PathFinder.get()).getConfigFileLoader().loadConfig();
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
                  PathFinder.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading configuration: ",
                          throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_CFG.formatted(
                      Messages.formatter().number("ms", System.currentTimeMillis() - now)
                  ));
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

          AbstractDiscoveryModule.<Player>getInstance().discover(target, group, LocalDateTime.now());

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

          AbstractDiscoveryModule.<Player>getInstance().forget(target, group);

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
