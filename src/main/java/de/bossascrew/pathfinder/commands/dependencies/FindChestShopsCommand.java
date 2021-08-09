package de.bossascrew.pathfinder.commands.dependencies;

import com.google.common.collect.Lists;
import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BossasCrewColors;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.bukkit.inventory.ModelDataHandler;
import de.bossascrew.core.bukkit.inventory.menu.PagedChestMenu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.bukkit.util.ItemStackUtils;
import de.bossascrew.core.player.PlayerHandler;
import de.bossascrew.pathfinder.PathPlugin;
import de.cubelegends.chestshoplogger.ChestShopLogger;
import de.cubelegends.chestshoplogger.models.ShopModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

@CommandAlias("finde|find")
public class FindChestShopsCommand extends BaseCommand {

	@Subcommand("chestshop")
	@Syntax("<Material>")
	@CommandPermission(PathPlugin.PERM_COMMAND_FIND_CHESTSHOPS)
	@CommandCompletion(BukkitMain.COMPLETE_MATERIALS_LOWERCASE)
	public void onChestShop(Player player, @Optional Material material) {

		World world = Bukkit.getWorld("skyblock_inseln");
		if (world == null) {
			return;
		}

		List<ShopModel> shops = ShopModel.getShops(ChestShopLogger.getInstance());
		shops = shops.stream()
				.filter(s -> material == null || s.getItemName().replaceAll(" ", "_").equalsIgnoreCase(material.toString()))
				.collect(Collectors.toList());

		//Alle Inseln filtern, zu denen man sich teleportieren kann.
		User user = BentoBox.getInstance().getPlayers().getUser(player.getUniqueId());
		Collection<Island> islands = BentoBox.getInstance().getIslands().getIslands().stream()
				.filter(Island::isOwned)
				.filter(island -> island.isAllowed(user, Flags.LOCK))
				.filter(island -> !island.isBanned(player.getUniqueId()))
				.collect(Collectors.toList());

		//Shops eine Insel zuordnen
		final Map<ShopModel, Island> filteredIslands = new HashMap<>();
		for (ShopModel shop : shops) {
			for (Island island : islands) {
				if (island.inIslandSpace(shop.getLoc())) {
					filteredIslands.put(shop, island);
					break;
				}
			}
		}
		if (filteredIslands.isEmpty()) {
			PlayerUtils.sendMessage(player, ChatColor.RED + "Kein Shop auf einer öffentlichen Insel gefunden, die dieses Item anbietet.");
			return;
		}
		AtomicBoolean sellOnly = new AtomicBoolean(false);
		openMenu(player, filteredIslands, sellOnly);
	}

	private void openMenu(Player player, Map<ShopModel, Island> filteredIslands, AtomicBoolean sellOnly) {
		int buyData = ModelDataHandler.getInstance().getModelData("chestshop-buy");
		int sellData = ModelDataHandler.getInstance().getModelData("chestshop-sell");

		PagedChestMenu menu = new PagedChestMenu(Component.text("Chestshops gefunden:"), 4);

		menu.setNavigationEntry(8, buildTypeIcon(sellOnly.get()), context -> {
			sellOnly.set(!sellOnly.get());
			openMenu(player, filteredIslands, sellOnly);
		});

		for (Map.Entry<ShopModel, Island> entry : filteredIslands.entrySet()) {
			ShopModel shop = entry.getKey();
			boolean sell = shop.getBuyPrice() == -1;
			if (sell != sellOnly.get()) {
				continue;
			}

			String name = PlayerHandler.getInstance().getGlobalPlayer(shop.getOwnerUUID()).getUsername();
			ItemStack icon = new ItemStack(Material.CHEST);
			ItemMeta meta = icon.getItemMeta();
			meta.displayName(Component.text(name, BossasCrewColors.SETTINGS_LIGHT_TEXT_COLOR).decoration(TextDecoration.ITALIC, false));
			meta.lore(Lists.newArrayList(
					Component.text(sell ? "Spieler können verkaufen" : "Spieler können einkaufen", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
					Component.text("Preis: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
							.append(Component.text(sell ? shop.getSellPrice() : shop.getBuyPrice(), NamedTextColor.YELLOW))
							.append(Component.text(" Dublonen", NamedTextColor.GOLD))));

			meta.setCustomModelData(sell ? sellData : buyData);
			icon.setItemMeta(meta);

			menu.addMenuEntry(icon, context -> {
				World w = Bukkit.getWorld("skyblock_inseln");
				if (w == null) {
					PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Die Welt skyblock_inseln existriert nicht.");
					return;
				}
				player.closeInventory();
				player.performCommand("is visit " + PlayerHandler.getInstance().getGlobalPlayer(entry.getValue().getOwner()).getUsername());
			});
		}
		menu.openInventory(player);
	}

	private ItemStack buildTypeIcon(boolean sell) {
		return ItemStackUtils.createItemStack(
				!sell ? Material.HOPPER : Material.DROPPER,
				!sell ? ChatColor.GREEN + "Einkaufen" : ChatColor.GOLD + "Verkaufen",
				ChatColor.GRAY + "Klicke, um zwischen Shoptypen zu wechseln");
	}
}
