package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.query.SearchTermHolder;

import java.util.Collection;

public interface NavigableModifier extends Modifier, SearchTermHolder {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:navigable");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  Collection<String> getSearchTermStrings();

  void removeSearchTermStrings(Collection<String> terms);

  void addSearchTermStrings(Collection<String> terms);

  void clearSearchTermStrings();
}
