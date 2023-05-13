package de.cubbossa.pathapi.dump;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * The DumpWriter interface creates a full report of the plugin, configuration, environment and data, to
 * achieve smooth data conversions between different major versions or to make error detection simpler.
 */
public interface DumpWriter {

  // header

  void setMainClass(Class<? extends PathFinder> mainClass);

  void setVersion(String version);

  void setModules(Collection<NamespacedKey> extensions);

  void addModule(NamespacedKey key);

  // environment

  void setServerSoftware(String software);

  void setPlugins(Collection<String> plugins);

  // configuration

  void setConfiguration(Map<String, Object> configuration);

  void setStyles(File file);

  void setTranslations(Collection<File> languageFiles);

  // data

  void setRegisteredNodeTypes(Collection<NamespacedKey> types);

  void setRegisteredVisualizerTypes(Collection<NamespacedKey> types);

  void setRegisteredModifierTypes(Collection<Class<Modifier>> types);

  void setNodeGroups(Collection<NodeGroup> groups);

  /**
   * Serializes all nodes, edges
   *
   * @param nodes a map of each node type with all loadable nodes and their according edges.
   */
  void setNodes(Map<NamespacedKey, Collection<Node>> nodes);

  void setVisualizers(Map<NamespacedKey, Collection<PathVisualizer<?, ?>>> visualizers);

  String toString();

  void save(File file);
}
