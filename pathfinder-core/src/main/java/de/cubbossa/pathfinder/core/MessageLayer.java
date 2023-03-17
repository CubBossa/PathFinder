package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.TranslationHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MessageLayer implements ApplicationLayer {

	private final CommandSender sender;
	private final ApplicationLayer subLayer;

	public EventsLayer eventLayer() {
		return new EventsLayer(this);
	}

	@Override
	public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
		return subLayer.createNodeGroup(key)
				.thenApply(group -> {
					TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(
							Placeholder.parsed("key", group.getKey().toString())
					), sender);
					return group;
				})
				.exceptionally(e -> {
					if (e instanceof IllegalArgumentException) {
						TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS.format(
								Placeholder.parsed("name", key.toString())
						), sender);
						return null;
					}
					TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE_FAIL, sender);
					e.printStackTrace();
					return null;
				});
	}

	@Override
	public CompletableFuture<List<NodeGroup>> getNodeGroups(Pagination pagination) {
		return subLayer.getNodeGroups(pagination).thenApply(nodeGroups -> {
			CommandUtils.printList(
					sender,
					pagination,
					this::getNodeGroups,
					group -> {
						TagResolver r = TagResolver.builder()
								.resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
								.resolver(Placeholder.component("size", Component.text(group.size())))
								.build();
						TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(r), sender);
					},
					Messages.CMD_NG_LIST_HEADER,
					Messages.CMD_NG_LIST_FOOTER
			);
			return nodeGroups;
		});
	}

	@Override
	public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
		return subLayer.deleteNodeGroup(key).thenApply(unused -> {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(
					Placeholder.parsed("name", key.toString())
			), sender);
			return null;
		});
	}
}
