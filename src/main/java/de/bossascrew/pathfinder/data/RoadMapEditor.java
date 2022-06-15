package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.findable.Node;
import de.cubbossa.menuframework.util.Pair;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class RoadMapEditor {

	private static final Vector ARMORSTAND_OFFSET = new Vector(0, -1.75, 0);
	private static final Vector ARMORSTAND_CHILD_OFFSET = new Vector(0, -1, 0);

	private final Map<UUID, HotbarMenu> editingPlayers;
	private EntityHider entityHider;

	private final Map<Node, ArmorStand> editModeNodeArmorStands;
	private final Map<Pair<Node, Node>, ArmorStand> editModeEdgeArmorStands;
	private int editModeTask = -1;

	private BukkitTask armorStandDistanceTask = null;


}
