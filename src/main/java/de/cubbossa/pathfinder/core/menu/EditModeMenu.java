package de.cubbossa.pathfinder.core.menu;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemoveEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapEditor;
import de.cubbossa.pathfinder.util.ClientNodeHandler;
import de.cubbossa.pathfinder.util.EditmodeUtils;
import de.cubbossa.pathfinder.util.ItemStackUtils;
import de.cubbossa.pathfinder.util.StringUtils;
import de.cubbossa.menuframework.inventory.*;
import de.cubbossa.menuframework.inventory.implementations.AnvilMenu;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.translations.TranslatedItem;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EditModeMenu {

	private final RoadMap roadMap;
	private Node edgeStart = null;
	private Node lastNode = null;
	private Boolean undirectedEdges = false;
	private NodeGroup lastGroup = null;
	private final Collection<NodeType<?>> types;

	public EditModeMenu(RoadMap roadMap, Collection<NodeType<?>> types) {
		this.roadMap = roadMap;
		this.types = types;
	}

	public BottomInventoryMenu createHotbarMenu(RoadMapEditor editor) {
		BottomInventoryMenu menu = new BottomInventoryMenu(InventoryRow.HOTBAR);

		menu.setDefaultClickHandler(Action.HOTBAR_DROP, c -> {
			Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> editor.setEditMode(c.getPlayer().getUniqueId(), false), 1L);
		});

		menu.setButton(0, Button.builder()
				.withItemStack(EditmodeUtils.NODE_TOOL)
				.withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
					Player p = context.getPlayer();
					roadMap.removeNodes(context.getTarget());
					p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1);
				})
				.withClickHandler(Action.RIGHT_CLICK_BLOCK, context -> {
					Vector pos = (context.getTarget()).getLocation().toVector().add(new Vector(0.5, 1.5, 0.5));

					if (types.size() <= 1) {
						NodeType<?> type = types.stream().findAny().orElse(null);
						if (type == null) {
							throw new IllegalStateException("Could not find any node type to generate node.");
						}
						lastNode = roadMap.createNode(type, pos, null, lastGroup);
					} else {
						openNodeTypeMenu(context.getPlayer(), pos);
					}
				}));


		menu.setButton(1, Button.builder()
				.withItemStack(() -> edgeStart == null ? EditmodeUtils.EDGE_TOOL : EditmodeUtils.EDGE_TOOL_GLOW)
				.withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, c -> {
					Player p = c.getPlayer();

					if (edgeStart == null) {
						edgeStart = c.getTarget();
					} else {
						if (edgeStart.equals(c.getTarget())) {
							p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							return;
						}
						if (edgeStart.getEdges().stream().anyMatch(e -> e.getEnd().equals(c.getTarget()))) {
							p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							return;
						}
						roadMap.connectNodes(edgeStart, c.getTarget(), !undirectedEdges, 1, 1);
						edgeStart = null;
					}
					c.getMenu().refresh(c.getSlot());
					p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 1, 1);
				})
				.withClickHandler(Action.LEFT_CLICK_AIR, context -> {
					Player player = context.getPlayer();

					// switch mode
					if (edgeStart == null) {
						undirectedEdges = !undirectedEdges;
						TranslationHandler.getInstance().sendMessage(Messages.E_EDGE_TOOL_DIR_TOGGLE
								.format(TagResolver.resolver("value", Tag.inserting(Messages.formatBool(!undirectedEdges)))), player);
						return;
					}
					// cancel creation
					edgeStart = null;
					TranslationHandler.getInstance().sendMessage(Messages.E_EDGE_TOOL_CANCELLED, player);
					player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_BREAK, 1, 1);
					context.getMenu().refresh(context.getSlot());

				})
				.withClickHandler(ClientNodeHandler.LEFT_CLICK_EDGE, context -> {
					Player player = context.getPlayer();
					roadMap.disconnectNodes(context.getTarget());
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
				})
				.withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
					Player player = context.getPlayer();
					roadMap.disconnectNode(context.getTarget());
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
				}));


		menu.setButton(8, Button.builder()
				.withItemStack(EditmodeUtils.TP_TOOL)
				.withClickHandler(context -> {
					double dist = -1;
					Node nearest = null;
					Vector vecP = context.getPlayer().getLocation().toVector();
					for (Node node : roadMap.getNodes()) {
						double d = node.getPosition().distance(vecP);
						if (dist == -1 || d < dist) {
							nearest = node;
							dist = d;
						}
					}
					if (nearest == null) {
						return;
					}
					Player p = context.getPlayer();
					Location newLoc = nearest.getLocation().setDirection(p.getLocation().getDirection());
					p.teleport(newLoc);
					p.playSound(newLoc, Sound.ENTITY_FOX_TELEPORT, 1, 1);
				}, Action.RIGHT_CLICK_ENTITY, Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR));

		menu.setButton(5, Button.builder()
				.withItemStack(EditmodeUtils.CURVE_TOOL)
				.withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context ->
						openTangentStrengthMenu(context.getPlayer(), context.getTarget())));

		menu.setButton(6, Button.builder()
				.withItemStack(EditmodeUtils.PERMISSION_TOOL)
				.withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context ->
						openNodePermissionMenu(context.getPlayer(), context.getTarget())));

		menu.setButton(2, Button.builder()
				.withItemStack(EditmodeUtils.GROUP_TOOL)
				.withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context -> {
					if (context.getTarget() instanceof Groupable groupable) {
						openGroupMenu(context.getPlayer(), groupable);
					}
				})
				.withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
					if (context.getTarget() instanceof Groupable groupable) {
						NodeGroupRemoveEvent event = new NodeGroupRemoveEvent(Lists.newArrayList(groupable), Lists.newArrayList(groupable.getGroups()));
						Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
							Bukkit.getPluginManager().callEvent(event);
							if (event.isCancelled()) {
								return;
							}
							event.getModifiedGroupables().forEach(g -> event.getModifiedGroups().forEach(g::removeGroup));
							NodeGroupRemovedEvent removed = new NodeGroupRemovedEvent(event.getModifiedGroupables(), event.getModifiedGroups());
							Bukkit.getPluginManager().callEvent(removed);

							context.getPlayer().playSound(context.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
						});
					}
				}));

		menu.setButton(3, Button.builder()
				.withItemStack(EditmodeUtils.LAST_GROUP_TOOL)
				.withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context -> {
					if (lastGroup != null && context.getTarget() instanceof Groupable groupable) {
						NodeGroupAssignEvent event = new NodeGroupAssignEvent(Lists.newArrayList(groupable), List.of(lastGroup));

						Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
							Bukkit.getPluginManager().callEvent(event);
							if (event.isCancelled()) {
								return;
							}
							event.getModifiedGroupables().forEach(g -> event.getModifiedGroups().forEach(g::addGroup));
							NodeGroupAssignedEvent assigned = new NodeGroupAssignedEvent(event.getModifiedGroupables(), event.getModifiedGroups());
							Bukkit.getPluginManager().callEvent(assigned);

							groupable.addGroup(lastGroup);
						});
					}
				}));

		return menu;
	}

	private void openGroupMenu(Player player, Groupable groupable) {

		ListMenu menu = new ListMenu(Messages.E_SUB_GROUP_TITLE.asTranslatable(), 4);
		menu.addPreset(MenuPresets.fillRow(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), 3)); //TODO extract icon
		for (NodeGroup group : NodeGroupHandler.getInstance().getNodeGroups()) {

			TagResolver resolver = TagResolver.builder()
					.resolver(Placeholder.component("name", group.getDisplayName()))
					.resolver(Placeholder.component("id", Messages.formatKey(group.getKey())))
					.resolver(Placeholder.parsed("name-format", group.getNameFormat()))
					.resolver(Placeholder.component("findable", Messages.formatBool(group.isDiscoverable())))
					.resolver(Placeholder.component("search-terms", Component.join(
							JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
							group.getSearchTerms().stream().map(Component::text).collect(Collectors.toList())
					)))
					.build();

			menu.addListEntry(Button.builder()
					.withItemStack(() -> {
						ItemStack stack = new TranslatedItem(group.isDiscoverable() ? Material.CHEST_MINECART : Material.FURNACE_MINECART,
								Messages.E_SUB_GROUP_ENTRY_N.format(resolver),
								Messages.E_SUB_GROUP_ENTRY_L.format(resolver)

						).createItem();
						if (groupable.getGroups().contains(group)) {
							stack = ItemStackUtils.setGlow(stack);
						}
						return stack;
					})
					.withClickHandler(Action.LEFT, c -> {
						if (!groupable.getGroups().contains(group)) {

							Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
								NodeGroupAssignEvent event = new NodeGroupAssignEvent(groupable, group);
								Bukkit.getPluginManager().callEvent(event);

								if (event.isCancelled()) {
									return;
								}

								event.getModifiedGroupables().forEach(g -> event.getModifiedGroups().forEach(g::addGroup));
								event.getModifiedGroups().forEach(g -> g.addAll(event.getModifiedGroupables()));

								NodeGroupAssignedEvent assigned = new NodeGroupAssignedEvent(event.getModifiedGroupables(), event.getModifiedGroups());
								Bukkit.getPluginManager().callEvent(assigned);

								c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
								menu.refresh(menu.getListSlots());
							});
						}
					})
					.withClickHandler(Action.RIGHT, c -> {
						if (groupable.getGroups().contains(group)) {

							Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
								NodeGroupRemoveEvent event = new NodeGroupRemoveEvent(groupable, group);
								Bukkit.getPluginManager().callEvent(event);

								if(event.isCancelled()) {
									return;
								}

								event.getModifiedGroupables().forEach(g -> event.getModifiedGroups().forEach(g::removeGroup));
								event.getModifiedGroups().forEach(g -> g.removeAll(event.getModifiedGroupables()));

								NodeGroupRemovedEvent removed = new NodeGroupRemovedEvent(event.getModifiedGroupables(), event.getModifiedGroups());
								Bukkit.getPluginManager().callEvent(removed);

								c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
								menu.refresh(menu.getListSlots());
							});
						}
					}));
		}
		menu.addPreset(presetApplier -> {
			presetApplier.addItemOnTop(3 * 9 + 7, new TranslatedItem(Material.BARRIER, Messages.E_SUB_GROUP_RESET_N, Messages.E_SUB_GROUP_RESET_L).createItem());
			presetApplier.addClickHandlerOnTop(3 * 9 + 7, Action.LEFT, c -> {
				groupable.clearGroups();
				menu.refresh(menu.getListSlots());
				c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f);
			});

			presetApplier.addItemOnTop(3 * 9 + 8, new TranslatedItem(Material.EMERALD, Messages.E_SUB_GROUP_NEW_N, Messages.E_SUB_GROUP_NEW_L).createItem());
			presetApplier.addClickHandlerOnTop(3 * 9 + 8, Action.LEFT, c -> {
				if (c.getMenu() instanceof TopInventoryMenu top) {
					top.openSubMenu(c.getPlayer(), newCreateGroupMenu(groupable));
				}
			});
		});
		menu.open(player);
	}

	private TopInventoryMenu newCreateGroupMenu(Groupable groupable) {
		AnvilMenu menu = newAnvilMenu(Component.text("Nodegruppe erstellen:"), "group", AnvilInputValidator.VALIDATE_KEY);

		menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
			NamespacedKey key = AnvilInputValidator.VALIDATE_KEY.getInputParser().apply(s.getTarget());
			if (key == null || NodeGroupHandler.getInstance().getNodeGroup(key) != null) {
				s.getPlayer().playSound(s.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			}
			NodeGroup group = NodeGroupHandler.getInstance().createNodeGroup(key, true, StringUtils.getRandHexString() + key.getKey());
			Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
				NodeGroupAssignEvent event = new NodeGroupAssignEvent(groupable, group);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					event.getGroupables().forEach(g -> event.getGroups().forEach(g::addGroup));
					NodeGroupAssignedEvent assigned = new NodeGroupAssignedEvent(event.getModifiedGroupables(), event.getModifiedGroups());
					Bukkit.getPluginManager().callEvent(assigned);
				}

				ListMenu prev = (ListMenu) menu.getPrevious(s.getPlayer());
				menu.openPreviousMenu(s.getPlayer());

				prev.refresh(prev.getListSlots());
			});
		});
		return menu;
	}

	private void openNodeTypeMenu(Player player, Vector pos) {

		ListMenu menu = new ListMenu(Component.text("Node-Gruppen verwalten:"), 2);
		for (NodeType<?> type : types) {

			menu.addListEntry(Button.builder()
					.withItemStack(type::getDisplayItem)
					.withClickHandler(Action.LEFT, c -> {
						lastNode = roadMap.createNode(type, pos, null, lastGroup);
						menu.close(player);
					}));
		}
		menu.open(player);
	}

	private void openTangentStrengthMenu(Player player, Node findable) {
		AnvilMenu menu = newAnvilMenu(Component.text("Rundung einstellen:"), "3.0", AnvilInputValidator.VALIDATE_FLOAT);

		menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
			if (!AnvilInputValidator.VALIDATE_FLOAT.getInputValidator().test(s.getTarget())) {
				s.getPlayer().playSound(s.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
				return;
			}
			Double strength = s.getTarget().equalsIgnoreCase("null") ? null : Double.parseDouble(s.getTarget());
			findable.setCurveLength(strength);
			menu.close(s.getPlayer());
		});
		menu.open(player);
	}

	private void openNodePermissionMenu(Player player, Node node) {
		AnvilMenu menu = newAnvilMenu(Component.text("Permission setzen:"), "null", AnvilInputValidator.VALIDATE_PERMISSION);
		menu.setItem(0, new ItemStack(Material.PAPER));
		menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
			Player p = s.getPlayer();
			node.setPermission(s.getTarget().equalsIgnoreCase("null") ? null : s.getTarget());
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
			menu.close(p);
		});
		menu.open(player);
	}

	public static AnvilMenu newAnvilMenu(ComponentLike title, String suggestion) {
		return newAnvilMenu(title, suggestion, null);
	}

	public static <T> AnvilMenu newAnvilMenu(ComponentLike title, String suggestion, AnvilInputValidator<T> validator) {
		AnvilMenu menu = new AnvilMenu(title, suggestion);
		menu.addPreset(MenuPresets.back(1, Action.LEFT));
		menu.setClickHandler(0, AnvilMenu.WRITE, s -> {
			if (validator != null && !validator.getInputValidator().test(s.getTarget())) {
				menu.setItem(2, ItemStackUtils.createErrorItem(Messages.GEN_GUI_WARNING_N, Messages.GEN_GUI_WARNING_L
						.format(TagResolver.resolver("format", Tag.inserting(validator.getRequiredFormat())))));
			} else {
				menu.setItem(2, ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_LETTER_CHECK_MARK, Messages.GEN_GUI_ACCEPT_N, Messages.GEN_GUI_ACCEPT_L));
			}
			menu.refresh(2);
		});
		return menu;
	}
}
