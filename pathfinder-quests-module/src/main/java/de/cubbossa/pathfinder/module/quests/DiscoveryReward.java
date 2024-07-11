package de.cubbossa.pathfinder.module.quests;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.discovery.BukkitDiscoveryModule;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.pikamug.quests.module.BukkitCustomReward;

public class DiscoveryReward extends BukkitCustomReward {

  public DiscoveryReward() {
    setName("PathFinder Discovery Reward");
    setAuthor("CubBossa");
    setItem("MAP", (short) 0);
    addStringPrompt("Places", "Enter a group or a list of groups that will be discovered.", null);
    setDisplay("Discovering: %Places%.");
  }

  @Override
  public void giveReward(UUID uuid, Map<String, Object> map) {
    var module = BukkitDiscoveryModule.getInstance();
    var player = PathPlayer.wrap(uuid);

    String concatenatedGroupList = (String) map.get("Places");
    List<String> groupNames = List.of(concatenatedGroupList.split(","));

    for (String groupName : groupNames) {
      NamespacedKey key;
      if (groupName.contains(":")) {
        key = NamespacedKey.fromString(groupName);
      } else {
        key = NamespacedKey.fromString("pathfinder:" + groupName);
      }
      PathFinder.get().getStorage().loadGroup(key).join()
          .ifPresent(group -> module.discover(player, group, LocalDateTime.now()));
    }
  }
}
