package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.configuration.Configuration;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PathFinderCommand extends CommandTree {

	public PathFinderCommand() {
		super("pathfinder");
		withPermission(PathPlugin.PERM_CMD_PF);

		then(new LiteralArgument("info").executes((commandSender, objects) -> {
			PluginDescriptionFile desc = PathPlugin.getInstance().getDescription();
			TranslationHandler.getInstance().sendMessage(Messages.INFO.format(TagResolver.builder()
					.resolver(Placeholder.unparsed("version", desc.getVersion()))
					.resolver(Placeholder.unparsed("api-version", desc.getAPIVersion() == null ? "none" : desc.getAPIVersion()))
					.resolver(Placeholder.unparsed("authors", String.join(",", desc.getAuthors())))
					.build()), commandSender);
		}));

		then(new LiteralArgument("help").executes((commandSender, objects) -> {
			TranslationHandler.getInstance().sendMessage(Messages.HELP, commandSender);
		}));

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
									.resolver(Placeholder.component("error", Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
									.build()), sender);
							PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading files: ", throwable);
						} else {
							TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS.format(TagResolver.builder()
									.resolver(Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
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
											.resolver(Placeholder.component("error", Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
											.build()), sender);
									PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading files: ", throwable);
								} else {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS_LANG.format(TagResolver.builder()
											.resolver(Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
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
											.resolver(Placeholder.component("error", Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
											.build()), sender);
									PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading files: ", throwable);
								} else {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS_FX.format(TagResolver.builder()
											.resolver(Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
											.build()), sender);
								}
							});
						})
				)
				.then(new LiteralArgument("config")
						.executes((sender, objects) -> {
							long now = System.currentTimeMillis();

							CompletableFuture.runAsync(() -> {
								try {
									PathPlugin.getInstance().setConfiguration(Configuration.loadFromFile(new File(PathPlugin.getInstance().getDataFolder(), "config.yml")));
								} catch (Throwable t) {
									throw new RuntimeException(t);
								}
							}).whenComplete((unused, throwable) -> {
								if (throwable != null) {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_ERROR.format(TagResolver.builder()
											.resolver(Placeholder.component("error", Component.text(throwable.getMessage().replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
											.build()), sender);
									PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Error occured while reloading configuration: ", throwable);
								} else {
									TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS_CFG.format(TagResolver.builder()
											.resolver(Placeholder.unparsed("ms", System.currentTimeMillis() - now + ""))
											.build()), sender);
								}
							});
						})
				)
		);
	}
}
