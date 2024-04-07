package de.cubbossa.pathapi.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerStorageImplementation;

/**
 * Extension point for implementation of custom PathVisualizers.
 * A PathVisualizer is responsible for creating a path view, which again renders a path across a graph
 * for a certain audience.
 * <p><p>
 * VisualizerTypes allow to create, store and load different instances of visualizers of that type,
 * which may have varying attributes. For example, a ParticleVisualizerType produces ParticleVisualizers.
 * ParticleVisualizers have properties like particle type, color, size, etc. One ParticleVisualizer creates
 * a View object with its own properties and the view renders the path.
 * <p/><p/>
 * When implementing this interface, you might want to use a custom storage, for example if visualizers
 * are backed by other data from your extension. If not, you may want to extend
 * {@link de.cubbossa.pathfinder.visualizer.AbstractVisualizerType} and {@link de.cubbossa.pathfinder.visualizer.AbstractVisualizer}.
 * AbstractVisalizerType implementations don't need to implement the storage but use the internal
 * PathFinder storage instead. You must provide a "serialize" and "deserialize" method to turn visualizers
 * into Maps of properties and back again.
 *
 * @param <VisualizerT> The type of the visualizer that is being handled by this type.
 */
public interface VisualizerType<VisualizerT extends PathVisualizer<?, ?>>
    extends Keyed, VisualizerStorageImplementation<VisualizerT>, Disposable {

  /**
   * Creates a new visualizer in storage.
   *
   * @param key The key to use for this visualizer. This key must be unique among all visualizers of all types.
   * @return The created visualizer instance.
   */
  VisualizerT createAndSaveVisualizer(NamespacedKey key);

  /**
   * A string representation in the ingame visualizer command. Mostly, it might be the key of the NamespacedKey.
   *
   * @return The string representation of the visualizer command section of this type.
   */
  default String getCommandName() {
    return getKey().getKey();
  }
}
