package de.cubbossa.pathfinder.module.quests;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.discovery.BukkitDiscoveryModule;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.pikamug.quests.module.BukkitCustomRequirement;

public class DiscoveryRequirement extends BukkitCustomRequirement {

  public DiscoveryRequirement() {
    setName("PathFinder Discovery Requirement");
    setAuthor("CubBossa");
    setItem("MAP", (short) 0);
    addStringPrompt("Places", "Enter a group or a list of groups that must have been discovered.", null);
    addStringPrompt("Count", "Ender a positive number or 0 (meaning 'all') to decide how many of the given groups must have been found", "");
    setDisplay("You must discover %Places%.");
  }


  @Override
  public boolean testRequirement(UUID uuid, Map<String, Object> map) {
    String concatenatedGroupList = (String) map.get("Places");
    int count = (int) map.get("Count");

    var module = BukkitDiscoveryModule.getInstance();

    List<String> groupNames = List.of(concatenatedGroupList.split(","));
    count = count == 0 ? groupNames.size() : count;

    int c = 0;
    int nc = groupNames.size();

    for (String groupName : groupNames) {
      NamespacedKey key;
      if (groupName.contains(":")) {
        key = NamespacedKey.fromString(groupName);
      } else {
        key = NamespacedKey.fromString("pathfinder:" + groupName);
      }
      NodeGroup group = PathFinder.get().getStorage().loadGroup(key).join().orElse(null);
      if (group != null && module.hasDiscovered(uuid, group).join()) {
        c++;
      } else {
        nc--;
      }
      if (nc < count) {
        return false;
      }
      if (c >= count) {
        return true;
      }
    }
    return false;
  }
}
