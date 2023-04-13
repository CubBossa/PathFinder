package de.cubbossa.pathfinder.module.visualizing.visualizer;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PathPluginExtension;

import java.util.function.Consumer;

import de.cubbossa.pathfinder.module.visualizing.VisualizerTypex;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

@AutoService(PathPluginExtension.class)
public class ScriptedVisualizerPathfinderExtension implements PathPluginExtension {

  public static final NamespacedKey KEY = new NamespacedKey(PathPlugin.getInstance(), "scriptline-visualizers");
  public static VisualizerTypex<de.cubbossa.pathfinder.module.visualizing.visualizer.ScriptLineParticleVisualizer> ADV_PARTICLE_VISUALIZER_TYPE =
      new ScriptLineParticleVisualizerType(
          new NamespacedKey(PathPlugin.getInstance(), "scriptline"));

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  @Override
  public void registerVisualizerType(Consumer<VisualizerTypex<? extends de.cubbossa.pathfinder.api.visualizer.PathVisualizer<?,?>>> typeConsumer) {
    typeConsumer.accept(ADV_PARTICLE_VISUALIZER_TYPE);
  }
}
