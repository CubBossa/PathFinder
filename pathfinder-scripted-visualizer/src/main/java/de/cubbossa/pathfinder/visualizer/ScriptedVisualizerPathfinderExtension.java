package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.implementation.VisualizerStorageImplementationWrapper;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

@Extension(points = PathFinderExtension.class)
public class ScriptedVisualizerPathfinderExtension extends PathFinderExtensionBase implements PathFinderExtension {

  public static final NamespacedKey KEY = AbstractPathFinder.pathfinder("scriptline-visualizers");
    public static AbstractVisualizerType<ScriptLineParticleVisualizer> ADV_PARTICLE_VISUALIZER_TYPE =
        new ScriptLineParticleVisualizerType();

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    if (pathPlugin.getStorage().getImplementation() instanceof InternalVisualizerStorageImplementation storage) {
      ADV_PARTICLE_VISUALIZER_TYPE.setStorage(new VisualizerStorageImplementationWrapper<>(ADV_PARTICLE_VISUALIZER_TYPE, storage));
    }
  }
}
