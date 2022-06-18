package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@CommandAlias("pathfinder")
public class PathFinderCommand extends BaseCommand {

	@Subcommand("reload language")
	@CommandPermission("pathfinder.command.pathfinder.reload")
	public void onReloadLanguage(CommandSender sender) {
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
				TranslationHandler.getInstance().sendMessage(Messages.RELOAD_SUCCESS.format(TagResolver.builder()
						.tag("ms", Tag.preProcessParsed(System.currentTimeMillis() - now + ""))
						.build()), sender);
			}
		});
	}
}
