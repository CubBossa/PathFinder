package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.CommandPermission;
import de.bossascrew.acf.annotation.Optional;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.core.bukkit.nbt.NBTEntity;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("pathfinder")
public class PathFinderCommand extends BaseCommand {

	@Subcommand("cleanup armorstands")
	@CommandPermission("bcrew.command.pathfinder.cleanup.armorstands")
	public void onCleanUp(CommandSender sender, @Optional World world) {

		List<ArmorStand> armorStands = new ArrayList<>();
		if(world != null) {
			armorStands.addAll(getArmorStands(world));
		}
		for(World w : Bukkit.getWorlds()) {
			armorStands.addAll(getArmorStands(w));
		}
		int count = 0;
		for(ArmorStand as : armorStands) {
			if(new NBTEntity(as).getPersistentDataContainer().hasKey(PathPlugin.NBT_ARMORSTAND_KEY)) {
				as.remove();
				count++;
			}
		}
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Es wurden " + count + " ungültige Rüstungsständer gefunden und entfernt.");
	}

	public List<ArmorStand> getArmorStands(World world) {
		return world.getEntities().stream()
				.filter(e -> e.getType() == EntityType.ARMOR_STAND)
				.map(e -> (ArmorStand) e)
				.collect(Collectors.toList());
	}
}
