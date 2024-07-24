package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.tinytranslations.MessageBundle;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ReloadCmd extends PathFinderSubCommand {

  public ReloadCmd(PathFinder pathFinder) {
    super(pathFinder, "reload");

    withPermission(PathPerms.PERM_CMD_PF_RELOAD);

    executes((sender, objects) -> {
      long now = System.currentTimeMillis();

      CompletableFuture.runAsync(() -> {
        AbstractPathFinder pf = BukkitPathFinder.getInstance();
        pf.setConfiguration(pf.getConfigFileLoader().loadConfig());

        MessageBundle translations = pf.getTranslations();

        Locale fallback = pf.getConfiguration().getLanguage().getFallbackLanguage();
        translations.clearCache();
        translations.writeLocale(fallback);
        translations.loadLocale(fallback);

        pf.getEventDispatcher().dispatchReloadEvent(true, true);

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
    });

    then(Arguments.literal("language")
        .executes((sender, objects) -> {
          long now = System.currentTimeMillis();

          CompletableFuture.runAsync(() -> {
            AbstractPathFinder pf = BukkitPathFinder.getInstance();
            MessageBundle translations = pf.getTranslations();

            Locale fallback = pf.getConfiguration().getLanguage().getFallbackLanguage();
            translations.clearCache();
            translations.writeLocale(fallback);
            translations.loadLocale(fallback);

            pf.getEventDispatcher().dispatchReloadEvent(false, true);

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
    );

    then(Arguments.literal("config")
        .executes((sender, objects) -> {
          long now = System.currentTimeMillis();

          CompletableFuture.runAsync(() -> {
            try {
              // TODO bah
              AbstractPathFinder pf = (AbstractPathFinder) PathFinder.get();
              pf.setConfiguration(pf.getConfigFileLoader().loadConfig());
            } catch (Throwable t) {
              throw new RuntimeException(t);
            }

            pathFinder.getEventDispatcher().dispatchReloadEvent(true, false);

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
    );
  }
}
