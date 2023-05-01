package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.DiscoverHandler;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.translations.TranslationHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * The basic command of this plugin, which handles things like reload, export, import, etc.
 */
public class PathFinderCommand extends Command {

  /**
   * The basic command of this plugin, which handles things like reload, export, import, etc.
   */
  public PathFinderCommand(PathFinder pathFinder) {
    super(pathFinder, "pathfinder");
    withAliases("pf");

    withRequirement(sender ->
        sender.hasPermission(PathPerms.PERM_CMD_PF_HELP)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_INFO)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_IMPORT)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_EXPORT)
            || sender.hasPermission(PathPerms.PERM_CMD_PF_RELOAD)
    );

    executes((sender, args) -> {
      TranslationHandler.getInstance().sendMessage(Messages.HELP.format(
              Placeholder.parsed("version", PathFinderProvider.get().getDescription().getVersion())
      ), sender);
    });

    then(CustomArgs.literal("info")
        .withPermission(PathPerms.PERM_CMD_PF_INFO)
        .executes((commandSender, objects) -> {
            PluginDescriptionFile desc = PathFinderProvider.get().getDescription();
          TranslationHandler.getInstance().sendMessage(Messages.INFO.format(TagResolver.builder()
              .resolver(Placeholder.unparsed("version", desc.getVersion()))
              .resolver(Placeholder.unparsed("api-version",
                  desc.getAPIVersion() == null ? "none" : desc.getAPIVersion()))
              .resolver(Placeholder.unparsed("authors", String.join(",", desc.getAuthors())))
              .build()), commandSender);
        }));

    then(CustomArgs.literal("modules")
        .withPermission(PathPerms.PERM_CMD_PF_MODULES)
        .executes((commandSender, args) -> {
          List<String> list =
                  PathFinderProvider.get().getExtensionRegistry().getExtensions().stream()
                  .map(PathFinderExtension::getKey)
                  .map(NamespacedKey::toString).toList();

          TranslationHandler.getInstance().sendMessage(Messages.MODULES.format(TagResolver.builder()
              .resolver(TagResolver.resolver("modules", Messages.formatList(list, Component::text)))
              .build()), commandSender);
        }));

    then(CustomArgs.literal("editmode")
        .executesPlayer((player, args) -> {
            NodeHandler.getInstance()
                    .toggleNodeGroupEditor(BukkitUtils.wrap(player), NodeHandler.GROUP_GLOBAL);
        })
        .then(CustomArgs.nodeGroupArgument("group")
            .executesPlayer((player, args) -> {
                NodeHandler.getInstance().toggleNodeGroupEditor(BukkitUtils.wrap(player),
                        ((SimpleNodeGroup) args[0]).getKey());
            })));

    then(CustomArgs.literal("help")
        .withPermission(PathPerms.PERM_CMD_PF_HELP)
        .executes((commandSender, objects) -> {
          TranslationHandler.getInstance().sendMessage(Messages.CMD_HELP, commandSender);
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
            try {
              TranslationHandler.getInstance().registerAnnotatedLanguageClass(Messages.class);
              TranslationHandler.getInstance().loadStyle();
              TranslationHandler.getInstance().loadLanguages();


            } catch (Throwable t) {
              throw new RuntimeException(t);
            }
          }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
              TranslationHandler.getInstance()
                  .sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
                      .resolver(Placeholder.component("error", Component.text(throwable.getMessage()
                          .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                      .build()), sender);
                PathFinderProvider.get().getLogger()
                  .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
            } else {
              TranslationHandler.getInstance()
                  .sendMessage(Messages.RELOAD_SUCCESS.format(TagResolver.builder()
                      .resolver(Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
                      .build()), sender);
            }
          });
        })

        .then(CustomArgs.literal("language")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                try {
                  TranslationHandler.getInstance().registerAnnotatedLanguageClass(Messages.class);
                  TranslationHandler.getInstance().loadStyle();
                  TranslationHandler.getInstance().loadLanguages();
                } catch (Throwable t) {
                  throw new RuntimeException(t);
                }
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
                          .resolver(Placeholder.component("error", Component.text(
                              throwable.getMessage()
                                  .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                          .build()), sender);
                    PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.RELOAD_SUCCESS_LANG.format(TagResolver.builder()
                          .resolver(
                              Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
                          .build()), sender);
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
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
                          .resolver(Placeholder.component("error", Component.text(
                              throwable.getMessage()
                                  .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                          .build()), sender);
                    PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading files: ", throwable);
                } else {
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.RELOAD_SUCCESS_FX.format(TagResolver.builder()
                          .resolver(
                              Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
                          .build()), sender);
                }
              });
            })
        )
        .then(CustomArgs.literal("config")
            .executes((sender, objects) -> {
              long now = System.currentTimeMillis();

              CompletableFuture.runAsync(() -> {
                try {
                    PathFinderProvider.get().loadConfig();
                } catch (Throwable t) {
                  throw new RuntimeException(t);
                }
              }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
                          .resolver(Placeholder.component("error", Component.text(
                              throwable.getMessage()
                                  .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
                          .build()), sender);
                    PathFinderProvider.get().getLogger()
                      .log(Level.SEVERE, "Error occured while reloading configuration: ",
                          throwable);
                } else {
                  TranslationHandler.getInstance()
                      .sendMessage(Messages.RELOAD_SUCCESS_CFG.format(TagResolver.builder()
                          .resolver(
                              Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
                          .build()), sender);
                }
              });
            })
        )
    );

    then(CustomArgs.literal("forcefind")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFIND)
        .then(CustomArgs.player("player")
            .withGeneratedHelp()
            .then(CustomArgs.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                    onForceFind(commandSender, args.getUnchecked(1), args.getUnchecked(2));
                }))));
    then(CustomArgs.literal("forceforget")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PF_FORCEFORGET)
        .then(CustomArgs.player("player")
            .withGeneratedHelp()
            .then(CustomArgs.discoverableArgument("discovering")
                .executes((commandSender, args) -> {
                  onForceForget(commandSender, (Player) args[1], (NamespacedKey) args[2]);
                }))));
  }

    private void onForceFind(CommandSender sender, PathPlayer<Player> target, NamespacedKey discoverable) {
        getPathfinder().getStorage().loadGroup(discoverable)
                .thenApply(Optional::orElseThrow)
                .thenAccept(group -> {
                    DiscoverableModifier mod = group.getModifier(DiscoverableModifier.class);

                    DiscoverHandler.getInstance().discover(target.getUniqueId(), group, LocalDateTime.now());

                    TranslationHandler.getInstance()
                            .sendMessage(Messages.CMD_RM_FORCE_FIND.format(TagResolver.builder()
                                    .resolver(Placeholder.unparsed("name", target.getName()))
                                    .resolver(Placeholder.component("name", target.getDisplayName()))
                  .tag("discovery", Tag.inserting(mod.getDisplayName())).build()), sender);
        });
  }

    private void onForceForget(PathPlayer<CommandSender> sender, PathPlayer<Player> target, NamespacedKey discoverable) {
        getPathfinder().getStorage().loadGroup(discoverable)
                .thenApply(Optional::orElseThrow)
                .thenAccept(group -> {
                    DiscoverableModifier mod = group.getModifier(DiscoverableModifier.class);

                    DiscoverHandler.getInstance().forget(target.getUniqueId(), group);

                    sender.sendMessage(Messages.CMD_RM_FORCE_FORGET.format(TagResolver.builder()
                            .resolver(Placeholder.unparsed("name", target.getName()))
                            .resolver(Placeholder.component("name", target.getDisplayName()))
                            .tag("discovery", Tag.inserting(mod.getDisplayName())).build()));
                });
  }
}
