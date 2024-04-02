package de.cubbossa.pathfinder.visualizer;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.implementation.InternalVisualizerStorageImplementationImpl;
import org.jetbrains.annotations.NotNull;

@AutoService(PathFinderExtension.class)
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
      ADV_PARTICLE_VISUALIZER_TYPE.setStorage(new InternalVisualizerStorageImplementationImpl<>(storage));
    }
  }
}
