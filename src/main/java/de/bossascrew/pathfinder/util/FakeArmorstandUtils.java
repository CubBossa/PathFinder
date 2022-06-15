package de.bossascrew.pathfinder.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import de.bossascrew.pathfinder.node.Waypoint;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

public class FakeArmorstandUtils {

	private Map<Integer, Waypoint> nodeArmorstands;
	private ProtocolManager protocolManager;

	public void spawnArmorstand(Player player, Location location) {

		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
		packet.getModifier().writeDefaults();
		packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
		packet.getIntegers().write(0, Entityid);
		packet.getUUIDs().write(0, UUID.randomUUID());
		packet.getIntegers().write(1, 1); //set the type of the entity,probably the problem?
		packet.getDoubles().write(0, location.getX());
		packet.getDoubles().write(1, location.getY());
		packet.getDoubles().write(2, location.getZ());
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void removeArmorstand(Player player, Integer id) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getModifier().writeDefaults();
		packet.getIntegers().write(0, 1);
		packet.getIntegerArrays().write(0, new int[]{id});
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void teleportArmorstand(Player player, Integer id, Location location) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
		packet.getModifier().writeDefaults();
		packet.getIntegers().write(0, id);
		packet.getDoubles().write(0, location.getX());
		packet.getDoubles().write(1, location.getY());
		packet.getDoubles().write(2, location.getZ());
		packet.getFloat().write(0, location.getYaw());
		packet.getFloat().write(1, location.getPitch());
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void renameArmorstand(Player player, Integer integer, String name) {

	}
}
