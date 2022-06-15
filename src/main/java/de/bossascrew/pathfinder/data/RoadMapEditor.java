package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.util.EditModeMenu;
import de.cubbossa.menuframework.util.Pair;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class RoadMapEditor {

	private static final Vector ARMORSTAND_OFFSET = new Vector(0, -1.75, 0);
	private static final Vector ARMORSTAND_CHILD_OFFSET = new Vector(0, -1, 0);

	private final Map<UUID, HotbarMenu> editingPlayers;
	private EntityHider entityHider;

	private final Map<Waypoint, ArmorStand> editModeNodeArmorStands;
	private final Map<Pair<Waypoint, Waypoint>, ArmorStand> editModeEdgeArmorStands;
	private int editModeTask = -1;

	private BukkitTask armorStandDistanceTask = null;


	/**
	 * @return true sobald mindestens ein Spieler den Editmode aktiv hat
	 */
	public boolean isEdited() {
		return !editingPlayers.isEmpty();
	}

	public void toggleEditMode(UUID uuid) {
		setEditMode(uuid, !isEditing(uuid));
	}

	public void cancelEditModes() {
		for (UUID uuid : editingPlayers.keySet()) {
			setEditMode(uuid, false);
		}
	}

	/**
	 * Setzt den Bearbeitungsmodus für einen Spieler, wobei auch Hotbarmenü etc gesetzt werden => nicht threadsafe
	 *
	 * @param uuid    des Spielers, dessen Modus gesetzt wird
	 * @param editing ob der Modus aktiviert oder deaktiviert wird
	 */
	public void setEditMode(UUID uuid, boolean editing) {
		Player player = Bukkit.getPlayer(uuid);
		PathPlayer editor = PathPlayerHandler.getInstance().getPlayer(uuid);
		if (editor == null) {
			return;
		}

		if (editing) {
			if (player == null) {
				return;
			}
			if (!isEdited()) {
				startEditModeVisualizer();
			}
			editor.setEditMode(roadmapId);
			HotbarMenu menu = new EditModeMenu(player, this).getHotbarMenu();
			editingPlayers.put(uuid, menu);
			menu.openInventory(player);
			player.setGameMode(GameMode.CREATIVE);
			toggleArmorStandsVisible(player, true);
		} else {
			if (player != null) {
				editingPlayers.get(uuid).closeInventory(player);
				toggleArmorStandsVisible(player, false);
			}

			editingPlayers.remove(uuid);
			editor.clearEditedRoadmap();

			if (!isEdited()) {
				stopEditModeVisualizer();
			}
		}
	}

	public void toggleArmorStandsVisible(Player player, boolean show) {
		if(show) {
			for(ArmorStand as : getEditModeEdgeArmorStands().values()) {
				entityHider.showEntity(player, as);
			}
			for(ArmorStand as : getEditModeNodeArmorStands().values()) {
				entityHider.showEntity(player, as);
			}
		} else {
			for(ArmorStand as : getEditModeEdgeArmorStands().values()) {
				entityHider.hideEntity(player, as);
			}
			for(ArmorStand as : getEditModeNodeArmorStands().values()) {
				entityHider.hideEntity(player, as);
			}
		}
	}

	public boolean isEditing(UUID uuid) {
		return editingPlayers.containsKey(uuid);
	}

	public boolean isEditing(Player player) {
		return isEditing(player.getUniqueId());
	}

	public void startEditModeVisualizer() {
		entityHider = new EntityHider(PathPlugin.getInstance(), EntityHider.Policy.BLACKLIST);

		for (Waypoint findable : nodes.values()) {
			if (!findable.getLocation().isChunkLoaded()) {
				continue;
			}
			if (findable instanceof NpcFindable) {
				continue;
			}
			ArmorStand nodeArmorStand = getNodeArmorStand(findable);
			editModeNodeArmorStands.put(findable, nodeArmorStand);
		}
		List<Pair<Waypoint, Waypoint>> processedFindables = new ArrayList<>();
		for (Pair<Waypoint, Waypoint> edge : edges) {
			if (processedFindables.contains(edge)) {
				continue;
			}
			ArmorStand edgeArmorStand = getEdgeArmorStand(edge);
			editModeEdgeArmorStands.put(edge, edgeArmorStand);
			processedFindables.add(edge);
		}
		updateEditModeParticles();

		armorStandDistanceTask = Bukkit.getScheduler().runTaskTimer(PathPlugin.getInstance(), () -> {
			for (UUID uuid : editingPlayers.keySet()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null || player.getWorld() != world) {
					continue;
				}
				List<ArmorStand> armorStands = new ArrayList<>(getEditModeNodeArmorStands().values());
				armorStands.addAll(getEditModeEdgeArmorStands().values());
				for (ArmorStand armorStand : armorStands) {
					if (player.getLocation().distance(armorStand.getLocation()) > 20) {
						entityHider.hideEntity(player, armorStand);
					} else {
						entityHider.showEntity(player, armorStand);
					}
				}
			}
		}, 10, 10);
	}

	public void stopEditModeVisualizer() {
		if (armorStandDistanceTask != null) {
			armorStandDistanceTask.cancel();
		}

		entityHider.destroy();
		entityHider = null;

		for (ArmorStand armorStand : editModeNodeArmorStands.values()) {
			armorStand.remove();
		}
		for (ArmorStand armorStand : editModeEdgeArmorStands.values()) {
			armorStand.remove();
		}
		editModeNodeArmorStands.clear();
		editModeEdgeArmorStands.clear();
		Bukkit.getScheduler().cancelTask(editModeTask);
	}

	public void updateArmorStandPosition(Waypoint findable) {
		ArmorStand as = editModeNodeArmorStands.get(findable);
		if (as == null) {
			return;
		}
		as.teleport(findable.getVector().toLocation(world).add(ARMORSTAND_OFFSET));

		for (Pair<Waypoint, Waypoint> edge : getEdges(findable)) {
			ArmorStand asEdge = editModeEdgeArmorStands.get(edge);
			if (asEdge == null) {
				return;
			}
			asEdge.teleport(getEdgeCenter(edge).add(ARMORSTAND_CHILD_OFFSET));
		}
	}

	public void updateArmorStandNodeHeads() {
		ItemStack head = HeadDBUtils.getHeadById(editModeVisualizer.getNodeHeadId());
		for (ArmorStand armorStand : editModeNodeArmorStands.values()) {
			armorStand.getEquipment().setHelmet(head);
		}
	}

	public void updateArmorStandEdgeHeads() {
		ItemStack head = HeadDBUtils.getHeadById(editModeVisualizer.getEdgeHeadId());
		for (ArmorStand armorStand : editModeEdgeArmorStands.values()) {
			armorStand.getEquipment().setHelmet(head);
		}
	}

	public void updateArmorStandDisplay(Waypoint findable) {
		updateArmorStandDisplay(findable, true);
	}

	public void updateArmorStandDisplay(Waypoint findable, boolean considerEdges) {
		ArmorStand as = editModeNodeArmorStands.get(findable);
		getNodeArmorStand(findable, as);

		if (!considerEdges) {
			return;
		}
		for (int edge : findable.getEdges()) {
			Pair<Waypoint, Waypoint> edgePair = getEdge(findable.getNodeId(), edge);
			if (edgePair == null) {
				continue;
			}
			getEdgeArmorStand(edgePair, editModeEdgeArmorStands.get(edgePair));
		}
	}



	/**
	 * Erstellt eine Liste aus Partikel Packets, die mithilfe eines Schedulers immerwieder an die Spieler im Editmode geschickt werden.
	 * Um gelöschte und neue Kanten darstellen zu können, muss diese Liste aus Packets aktualisiert werden.
	 * Wird asynchron ausgeführt
	 */
	public void updateEditModeParticles() {
		PluginUtils.getInstance().runSync(() -> {

			//Bestehenden Task cancellen
			Bukkit.getScheduler().cancelTask(editModeTask);

			//Packet List erstellen, die dem Spieler dann wieder und wieder geschickt wird. (Muss refreshed werden, wenn es Änderungen gibt.)
			List<Object> packets = new ArrayList<>();
			ParticleBuilder particle = new ParticleBuilder(ParticleEffect.valueOf(editModeVisualizer.getParticle().toString()))
					.setColor(java.awt.Color.RED);

			//Alle linearen Verbindungen der Waypoints errechnen und als Packet sammeln. Berücksichtigen, welche Node schon behandelt wurde, um doppelte Geraden zu vermeiden
			List<Pair<Waypoint, Waypoint>> processedFindables = new ArrayList<>();
			for (Pair<Waypoint, Waypoint> edge : edges) {
				if (processedFindables.contains(edge)) {
					continue;
				}
				List<Vector> points = BezierUtils.getBezierCurveDistanced(editModeVisualizer.getParticleDistance(), edge.first.getVector(), edge.second.getVector());
				packets.addAll(points.stream()
						.map(vector -> vector.toLocation(world))
						.map(location -> particle.setLocation(location).toPacket())
						.collect(Collectors.toSet()));
				processedFindables.add(edge);
			}
			if (packets.size() > editModeVisualizer.getParticleLimit()) {
				packets = packets.subList(0, editModeVisualizer.getParticleLimit());
			}
			final List<Object> fPackets = packets;
			editModeTask = TaskManager.startSuppliedTask(fPackets, editModeVisualizer.getSchedulerPeriod(), () -> editingPlayers.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline).collect(Collectors.toSet()));
		});
	}

	private ArmorStand getNodeArmorStand(Waypoint findable) {
		return getNodeArmorStand(findable, null);
	}

	private ArmorStand getNodeArmorStand(Waypoint findable, ArmorStand toEdit) {
		String name = findable.getNameFormat() + " #" + findable.getNodeId() +
				(findable.getGroup() == null ? "" : (findable.getGroup().isFindable() ? ChatColor.GRAY : ChatColor.DARK_GRAY) + " (" + findable.getGroup().getName() + ")");

		if (toEdit == null) {
			toEdit = getNewArmorStand(findable.getLocation().clone().add(ARMORSTAND_OFFSET), name, editModeVisualizer.getNodeHeadId());
		} else {
			toEdit.setCustomName(name);
			toEdit.getEquipment().setHelmet(HeadDBUtils.getHeadById(editModeVisualizer.getNodeHeadId()));
		}
		return toEdit;
	}

	private ArmorStand getEdgeArmorStand(Pair<Waypoint, Waypoint> edge) {
		return getEdgeArmorStand(edge, null);
	}

	private ArmorStand getEdgeArmorStand(Pair<Waypoint, Waypoint> edge, ArmorStand toEdit) {
		//String name = edge.first.getName() + " (#" + edge.first.getDatabaseId() + ") ↔ " + edge.second.getName() + " (#" + edge.second.getDatabaseId() + ")";

		if (toEdit == null) {
			toEdit = getNewArmorStand(getEdgeCenter(edge).add(ARMORSTAND_CHILD_OFFSET), null, editModeVisualizer.getEdgeHeadId(), true);
		} else {
			toEdit.setCustomName(null);
			toEdit.getEquipment().setHelmet(HeadDBUtils.getHeadById(editModeVisualizer.getEdgeHeadId()));
		}
		toEdit.setSmall(true);
		return toEdit;
	}

	public void setEditModeVisualizer(EditModeVisualizer editModeVisualizer) {
		if (this.editModeVisualizer != null) {
			this.editModeVisualizer.getNodeHeadSubscribers().unsubscribe(this.getRoadmapId());
			this.editModeVisualizer.getEdgeHeadSubscribers().unsubscribe(this.getRoadmapId());
			this.editModeVisualizer.getUpdateParticle().unsubscribe(this.getRoadmapId());
		}
		this.editModeVisualizer = editModeVisualizer;
		updateData();

		this.editModeVisualizer.getNodeHeadSubscribers().subscribe(this.getRoadmapId(), integer -> PluginUtils.getInstance().runSync(() -> {
			if (isEdited()) {
				this.updateArmorStandNodeHeads();
			}
		}));
		this.editModeVisualizer.getEdgeHeadSubscribers().subscribe(this.getRoadmapId(), integer -> PluginUtils.getInstance().runSync(() -> {
			if (isEdited()) {
				this.updateArmorStandEdgeHeads();
			}
		}));
		this.editModeVisualizer.getUpdateParticle().subscribe(this.getRoadmapId(), obj -> {
			if (isEdited()) {
				updateEditModeParticles();
			}
		});

		if (isEdited()) {
			updateArmorStandEdgeHeads();
			updateArmorStandNodeHeads();
			updateEditModeParticles();
		}
	}

	public void updateChunkArmorStands(Chunk chunk, boolean unload) {
		if (!isEdited()) {
			return;
		}
		List<Waypoint> nodes = new ArrayList<>(getNodes());
		nodes = nodes.stream().filter(node -> node.getLocation().getChunk().equals(chunk)).collect(Collectors.toList());
		for (Waypoint findable : nodes) {
			if (findable instanceof NpcFindable) {
				continue;
			}
			//TODO edges
			if (unload) {
				getNodeArmorStand(findable).remove();
				editModeNodeArmorStands.remove(findable);
			} else {
				ArmorStand nodeArmorStand = getNodeArmorStand(findable);
				editModeNodeArmorStands.put(findable, nodeArmorStand);
			}
		}
	}

	private Location getEdgeCenter(Pair<Waypoint, Waypoint> edge) {
		Waypoint a = edge.first;
		Waypoint b = edge.second;

		if (edge.first != null && edge.second != null) {
			Vector va = a.getVector().clone();
			Vector vb = b.getVector().clone();
			return va.add(vb.subtract(va).multiply(0.5)).toLocation(world);
		}
		return null;
	}

	private ArmorStand getNewArmorStand(Location location, String name, int headDbId) {
		return getNewArmorStand(location, name, headDbId, false);
	}

	private ArmorStand getNewArmorStand(Location location, @Nullable String name, int headDbId, boolean small) {
		ArmorStand as = location.getWorld().spawn(location,
				ArmorStand.class,
				armorStand -> {
					entityHider.hideEntity(armorStand);
					for (UUID uuid : editingPlayers.keySet()) {
						entityHider.showEntity(Bukkit.getPlayer(uuid), armorStand);
					}
					armorStand.setVisible(false);
					if (name != null) {
						armorStand.setCustomNameVisible(true);
						armorStand.setCustomName(name);
					}
					armorStand.setGravity(false);
					armorStand.setInvulnerable(true);
					armorStand.setSmall(small);
					ItemStack helmet = HeadDBUtils.getHeadById(headDbId);
					if (armorStand.getEquipment() != null && helmet != null) {
						armorStand.getEquipment().setHelmet(helmet);
					}
				});

		NBTEntity e = new NBTEntity(as);
		e.getPersistentDataContainer().addCompound(PathPlugin.NBT_ARMORSTAND_KEY);
		return as;
	}
}
