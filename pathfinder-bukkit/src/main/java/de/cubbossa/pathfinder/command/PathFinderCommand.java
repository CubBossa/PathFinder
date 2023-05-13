package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.AbstractDiscoverHandler;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.translations.PluginTranslations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
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
public class PathFinderCommand extends Command {

  /**
   * The basic command of this plugin, which handles things like reload, export, import, etc.
   */
  public PathFinderCommand(PathFinder pathFinder) {
    super(pathFinder, "de/cubbossa/pathfinder");
    withAliases("pf");

    withRequirement(sender ->
        sender.hasPermission(PathPerms.PERM_CMD_PF_HELP)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_INFO)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_IMPORT)
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

          BukkitUtils.wrap(commandSender).sendMessage(Messages.MODULES.formatted(TagResolver.builder()
              .resolver(TagResolver.resolver("modules", Messages.formatList(list, Component::text)))
              .build()));
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

    then(CustomArgs.literal("import")
        .withPermission(PathPerms.PERM_CMD_PF_IMPORT)
        .then(new VisualizerImportCommand(pathFinder, "visualizer", 0))
    );

    then(CustomArgs.literal("reload")
        .withPermission(PathPerms.PERM_CMD_PF_RELOAD)

        .executes((sender, objects) -> {
          long now = System.currentTimeMillis();

          CompletableFuture.runAsync(() -> {
            CommonPathFinder pf = BukkitPathFinder.getInstance();
            PluginTranslations translations = pf.getTranslations();

            translations.clearCache();
            translations.writeLocale(Locale.ENGLISH); // TODO
            translations.loadLocale(Locale.ENGLISH);
          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(TagResolver.builder()
                  .resolver(Placeholder.component("error", Component.text(throwable.getMessage()
                      .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                  .build()));
              PathFinderProvider.get().getLogger()
                  .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
            } else {
              BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS.formatted(TagResolver.builder()
                  .resolver(Placeholder.unparsed("ms", String.valueOf(System.currentTimeMillis() - now)))
                  .build()));
            }
          });
        })

        .then(CustomArgs.literal("language")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                CommonPathFinder pf = BukkitPathFinder.getInstance();
                PluginTranslations translations = pf.getTranslations();

                translations.clearCache();
                translations.writeLocale(Locale.ENGLISH); // TODO
                translations.loadLocale(Locale.ENGLISH);
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(TagResolver.builder()
                      .resolver(Placeholder.component("error", Component.text(throwable.getMessage()
                          .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                      .build()));
                  PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_LANG.formatted(TagResolver.builder()
                      .resolver(Placeholder.unparsed("ms", String.valueOf(System.currentTimeMillis() - now)))
                      .build()));
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
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_FX.formatted(TagResolver.builder()
                      .resolver(Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
                      .build()));
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
                  BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_SUCCESS_CFG.formatted(TagResolver.builder()
                      .resolver(
                          Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
                      .build()));
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
                  onForceFind(commandSender, args.getUnchecked(1), args.getUnchecked(2));
                }))));
    then(CustomArgs.literal("forceforget")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFORGET)
        .then(CustomArgs.pathPlayer("player")
            .withGeneratedHelp()
            .then(CustomArgs.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                  onForceForget(BukkitUtils.wrap(commandSender), args.getUnchecked(1), args.getUnchecked(2));
                }))));
  }

  private void onForceFind(CommandSender sender, PathPlayer<Player> target, NamespacedKey discoverable) {
    getPathfinder().getStorage().loadGroup(discoverable)
        .thenApply(Optional::orElseThrow)
        .thenAccept(group -> {
          DiscoverableModifier mod = group.getModifier(DiscoverableModifier.class);

          AbstractDiscoverHandler.getInstance().discover(target.getUniqueId(), group, LocalDateTime.now());

          BukkitUtils.wrap(sender).sendMessage(Messages.CMD_RM_FORCE_FIND.formatted(TagResolver.builder()
              .resolver(Placeholder.unparsed("name", target.getName()))
              .resolver(Placeholder.component("name", target.getDisplayName()))
              .tag("discovery", Tag.inserting(mod.getDisplayName())).build()));
        });
  }

  private void onForceForget(PathPlayer<CommandSender> sender, PathPlayer<Player> target, NamespacedKey discoverable) {
    getPathfinder().getStorage().loadGroup(discoverable)
        .thenApply(Optional::orElseThrow)
        .thenAccept(group -> {
          DiscoverableModifier mod = group.getModifier(DiscoverableModifier.class);

          AbstractDiscoverHandler.getInstance().forget(target.getUniqueId(), group);

          sender.sendMessage(Messages.CMD_RM_FORCE_FORGET.formatted(TagResolver.builder()
              .resolver(Placeholder.unparsed("name", target.getName()))
              .resolver(Placeholder.component("name", target.getDisplayName()))
              .tag("discovery", Tag.inserting(mod.getDisplayName())).build()));
        });
  }
}
