package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.configuration.Configuration;
import de.cubbossa.pathfinder.data.DataExporter;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.SqliteDatabase;
import de.cubbossa.pathfinder.data.YmlDatabase;
import de.cubbossa.pathfinder.module.visualizing.command.VisualizerImportCommand;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
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

		then(new LiteralArgument("export")
				.withPermission(PathPlugin.PERM_CMD_PF_EXPORT)
				.then(new LiteralArgument("sqlite")
						.then(new TextArgument("filename")
								.executes((commandSender, objects) -> {
									PathPlugin pl = PathPlugin.getInstance();
									long now = System.currentTimeMillis();
									Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
										String fileName = (String) objects[0];
										if (!fileName.endsWith(".db")) {
											fileName = fileName + ".db";
										}
										DataStorage storage = new SqliteDatabase(new File(pl.getDataFolder(), "exports/" + fileName));
										try {
											storage.connect();
											DataExporter.all().save(storage);
										} catch (IOException e) {
											pl.getLogger().log(Level.SEVERE, e, e::getMessage);
											commandSender.sendMessage("Complete in " + (System.currentTimeMillis() - now) + "ms.");
										}
									});
								})
						)
				)
				.then(new LiteralArgument("yaml")
						.then(new TextArgument("directory")
								.executes((commandSender, objects) -> {
									PathPlugin pl = PathPlugin.getInstance();
									long now = System.currentTimeMillis();
									Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
										String dir = (String) objects[0];
										if (!dir.startsWith("/")) {
											dir = "/" + dir;
										}
										File directory = new File(pl.getDataFolder(), "exports/" + dir);
										directory.mkdirs();
										DataStorage storage = new YmlDatabase(directory);
										try {
											storage.connect();
											DataExporter.all().save(storage);
											commandSender.sendMessage("Complete in " + (System.currentTimeMillis() - now) + "ms.");
										} catch (IOException e) {
											pl.getLogger().log(Level.SEVERE, e, e::getMessage);
										}
									});
								})
						)
				)
		);

		then(new LiteralArgument("import")
				.withPermission(PathPlugin.PERM_CMD_PF_IMPORT)
				.then(new VisualizerImportCommand(new LiteralArgument("visualizer"), 0))
		);

		then(new LiteralArgument("reload")
				.withPermission(PathPlugin.PERM_CMD_PF_RELOAD)

				.executes((sender, objects) -> {
					long now = System.currentTimeMillis();

					CompletableFuture.runAsync(() -> {
						try {
							TranslationHandler.getInstance().registerAnnotatedLanguageClass(Messages.class);
							TranslationHandler.getInstance().loadStyle();
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
									TranslationHandler.getInstance().loadStyle();
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
									PathPlugin.getInstance().generateIfAbsent("effects.nbo");
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
