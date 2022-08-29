package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PathFinderCommand extends CommandTree {

	public PathFinderCommand() {
		super("pathfinder");

		then(new LiteralArgument("reload")
				.withPermission(PathPlugin.PERM_CMD_PF_RELOAD)

				.executes((sender, objects) -> {
					long now = System.currentTimeMillis();

					CompletableFuture.runAsync(() -> {
						try {
							TranslationHandler.getInstance().registerAnnotatedLanguageClass(Messages.class);
							TranslationHandler.getInstance().loadLanguages();

							EffectHandler.getInstance().clearCache(PathPlugin.getInstance().getEffectsFile());


						} catch (Throwable t) {
							throw new RuntimeException(t);
						}
					}).whenComplete((unused, throwable) -> {
						if (throwable != null) {
							TranslationHandler.getInstance().sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
									.tag("error", Tag.inserting(Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
									.build()), sender);
							PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading files: ", throwable);
						} else {
							TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS.format(TagResolver.builder()
									.tag("ms", Tag.preProcessParsed(System.currentTimeMillis() - now + ""))
									.build()), sender);
						}
					});
				})

				.then(new LiteralArgument("language")
						.executes((sender, objects) -> {
							long now = System.currentTimeMillis();

							CompletableFuture.runAsync(() -> {
								try {
									TranslationHandler.getInstance().registerAnnotatedLanguageClass(Messages.class);
									TranslationHandler.getInstance().loadLanguages();
								} catch (Throwable t) {
									throw new RuntimeException(t);
								}
							}).whenComplete((unused, throwable) -> {
								if (throwable != null) {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
											.tag("error", Tag.inserting(Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
											.build()), sender);
									PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading files: ", throwable);
								} else {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS_LANG.format(TagResolver.builder()
											.tag("ms", Tag.preProcessParsed(System.currentTimeMillis() - now + ""))
											.build()), sender);
								}
							});
						})
				)

				.then(new LiteralArgument("effects")
						.executes((sender, objects) -> {
							long now = System.currentTimeMillis();

							CompletableFuture.runAsync(() -> {
								try {
									EffectHandler.getInstance().clearCache(PathPlugin.getInstance().getEffectsFile());
								} catch (Throwable t) {
									throw new RuntimeException(t);
								}
							}).whenComplete((unused, throwable) -> {
								if (throwable != null) {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
											.tag("error", Tag.inserting(Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
											.build()), sender);
									PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading files: ", throwable);
								} else {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS_FX.format(TagResolver.builder()
											.tag("ms", Tag.preProcessParsed(System.currentTimeMillis() - now + ""))
											.build()), sender);
								}
							});
						})
				)

				.then(new LiteralArgument("config")
						.executes((commandSender, objects) -> {

						})
				)
		);
	}
}
