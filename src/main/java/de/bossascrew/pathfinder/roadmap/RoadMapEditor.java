package de.bossascrew.pathfinder.roadmap;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.util.EditModeMenu;
import de.bossascrew.pathfinder.util.FakeArmorstandHandler;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class RoadMapEditor implements Keyed {

	private final NamespacedKey key;
	private final RoadMap roadMap;
	private final FakeArmorstandHandler armorstandHandler;

	private final Map<UUID, BottomInventoryMenu> editingPlayers;
	private final Map<UUID, GameMode> preservedGameModes;

	private int editModeTask = -1;

	public RoadMapEditor(RoadMap roadMap) {
		this.key = roadMap.getKey();
		this.roadMap = roadMap;

		this.armorstandHandler = new FakeArmorstandHandler(PathPlugin.getInstance());
		this.editingPlayers = new HashMap<>();
		this.preservedGameModes = new HashMap<>();
	}

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
	 * Sets a player into edit mode for this roadmap.
	 *
	 * @param uuid     the player to set the edit mode for
	 * @param activate activate or deactivate edit mode
	 */
	public void setEditMode(UUID uuid, boolean activate) {
		Player player = Bukkit.getPlayer(uuid);
		PathPlayer editor = PathPlayerHandler.getInstance().getPlayer(uuid);
		if (editor == null) {
			return;
		}

		if (activate) {
			if (player == null) {
				return;
			}
			if (!isEdited()) {
				startParticleTask();
			}
			editor.setEditMode(key);

			BottomInventoryMenu menu = new EditModeMenu(player, this).getHotbarMenu();
			editingPlayers.put(uuid, menu);
			menu.openSync(player);

			preservedGameModes.put(player.getUniqueId(), player.getGameMode());
			player.setGameMode(GameMode.CREATIVE);

			showArmorStands(player);
		} else {

			if (player != null) {
				BottomInventoryMenu menu = editingPlayers.get(uuid);
				if (menu != null) {
					menu.close(player);
				}
				hideArmorStands(player);
				player.setGameMode(preservedGameModes.getOrDefault(player.getUniqueId(), GameMode.SURVIVAL));
			}

			editingPlayers.remove(uuid);
			editor.clearEditedRoadmap();

			if (!isEdited()) {
				stopParticleTask();
			}
		}
	}

	public void showArmorStands(Player player) {
		armorstandHandler.showNodes(roadMap.getNodes(), player);
		armorstandHandler.showEdges(roadMap.getEdges(), player);
	}

	public void hideArmorStands(Player player) {
		armorstandHandler.hideNodes(roadMap.getNodes(), player);
		armorstandHandler.hideEdges(roadMap.getEdges(), player);
	}

	public boolean isEditing(UUID uuid) {
		return editingPlayers.containsKey(uuid);
	}

	public boolean isEditing(Player player) {
		return isEditing(player.getUniqueId());
	}

	private void startParticleTask() {

	}

	private void stopParticleTask() {

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
}
