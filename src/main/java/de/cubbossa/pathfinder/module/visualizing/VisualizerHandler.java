package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.events.ParticleVisualizerStepsChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.ParticleVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.Visualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.wrappers.ParticleData;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class VisualizerHandler {

	public static final VisualizerType<ParticleVisualizer> PARTICLE_VISUALIZER_TYPE = new VisualizerType<ParticleVisualizer>(new NamespacedKey(PathPlugin.getInstance(), "particle")) {

		@Override
		public Message getInfoMessage(ParticleVisualizer element) {
			return Messages.CMD_VIS_INFO_PARTICLES.format(TagResolver.builder()
					.tag("particle", Tag.inserting(Messages.formatParticle(element.getParticle(), element.getParticleData())))
					.tag("particle-steps", Tag.inserting(Component.text(element.getSchedulerSteps())))
					.tag("amount", Tag.inserting(Component.text(element.getAmount())))
					.tag("speed", Tag.inserting(Component.text(element.getSpeed())))
					.tag("offset", Tag.inserting(Messages.formatVector(element.getOffset())))
					.build());
		}

		@Override
		public void appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
			tree
					.then(new LiteralArgument("particle")
							.then(new ParticleArgument("particle")
									.executes((commandSender, objects) -> {
										ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
										onSetParticle(visualizer, (ParticleData) objects[argumentOffset], null, null, null);
									})
									.then(new IntegerArgument("amount", 1)
											.executes((commandSender, objects) -> {
												ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
												onSetParticle(visualizer, (ParticleData) objects[argumentOffset], (Integer) objects[argumentOffset + 1], null, null);
											})
											.then(new FloatArgument("speed", 0)
													.executes((commandSender, objects) -> {
														ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
														onSetParticle(visualizer, (ParticleData) objects[argumentOffset], (Integer) objects[argumentOffset + 1], (Float) objects[argumentOffset + 2], null);
													})
													.then(new LocationArgument("offset")
															.executes((commandSender, objects) -> {
																ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
																onSetParticle(visualizer, (ParticleData) objects[argumentOffset], (Integer) objects[argumentOffset + 1], (Float) objects[argumentOffset + 2], ((Location) objects[argumentOffset + 3]).toVector());
															})
													)
											)
									)
							))
					.then(new LiteralArgument("particle-steps")
							.then(new IntegerArgument("amount", 1)
									.executes((commandSender, objects) -> {
										ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
										int amount = (int) objects[argumentOffset];
										onSetSteps(visualizer, amount);
									})
							)
					);
		}


		private <T> void onSetSteps(ParticleVisualizer visualizer, int amount) {
			int old = visualizer.getSchedulerSteps();
			visualizer.setSchedulerSteps(amount);
			Bukkit.getPluginManager().callEvent(new ParticleVisualizerStepsChangedEvent(visualizer, old, amount));
		}

		private <T> void onSetParticle(ParticleVisualizer visualizer, ParticleData<T> particle, @Nullable Integer amount, @Nullable Float speed, @Nullable Vector offset) {
			visualizer.setParticle(particle.particle());
			if (particle.data() != null) {
				visualizer.setParticleData(particle.data());
			} else {
				visualizer.setParticleData(null);
			}
			if (amount != null) {
				visualizer.setAmount(amount);
			}
			if (speed != null) {
				visualizer.setSpeed(speed);
			}
			if (offset != null) {
				visualizer.setOffset(offset);
			}
		}
	};
	@Getter
	private static VisualizerHandler instance;

	private final HashedRegistry<VisualizerType<?>> visualizerTypes;

	private final PathVisualizer<?> defaultVisualizer;
	private final HashedRegistry<PathVisualizer<?>> pathVisualizerMap;

	// Map<Player, Map<RoadMap, PathVisualizer>>
	private final Map<UUID, Map<NamespacedKey, PathVisualizer<?>>> playerVisualizers;
	private final Map<Integer, HashedRegistry<PathVisualizer<?>>> roadmapVisualizers;


	public VisualizerHandler() {

		instance = this;
		ParticleVisualizer defaultVis = new ParticleVisualizer(new NamespacedKey(PathPlugin.getInstance(), "default"), "Default");
		defaultVis.setParticle(Particle.SCRAPE);
		defaultVis.setSchedulerSteps(50);
		defaultVis.setPointDistance(0.12f);
		defaultVis.setAmount(1);
		defaultVis.setSpeed(.5f);
		defaultVis.setOffset(new Vector(0.02f, 0.02f, 0.02f));
		defaultVisualizer = defaultVis;

		this.visualizerTypes = new HashedRegistry<>();
		visualizerTypes.put(PARTICLE_VISUALIZER_TYPE);

		this.pathVisualizerMap = new HashedRegistry<>();
		pathVisualizerMap.put(defaultVisualizer);
		this.playerVisualizers = new HashMap<>();
		this.roadmapVisualizers = new HashMap<>();
	}

	public @Nullable <T extends PathVisualizer<T>> VisualizerType<T> getVisualizerType(NamespacedKey key) {
		return (VisualizerType<T>) visualizerTypes.get(key);
	}

	public <T extends PathVisualizer<T>> void registerVisualizerType(VisualizerType<T> type) {
		visualizerTypes.put(type);
	}

	public void unregisterVisualizerType(VisualizerType<?> type) {
		visualizerTypes.remove(type.getKey());
	}

	public void setSteps(Visualizer<?> visualizer, int value) {
		int old = visualizer.getSchedulerSteps();
		visualizer.setSchedulerSteps(value);
		Bukkit.getPluginManager().callEvent(new ParticleVisualizerStepsChangedEvent(visualizer, old, value));
	}

	public @Nullable PathVisualizer<?> getPathVisualizer(NamespacedKey key) {
		return pathVisualizerMap.get(key);
	}

	public PathVisualizer<?> createPathVisualizer(NamespacedKey key) {
		return null; //TODO
	}

	public PathVisualizer<?> createPathVisualizer(NamespacedKey key,
												  String nameFormat,
												  ParticleBuilder particle,
												  ItemStack displayItem,
												  double particleDistance,
												  int particleSteps,
												  int schedulerPeriod) {

		if (pathVisualizerMap.containsKey(key)) {
			throw new IllegalArgumentException("Could not insert new path visualizer, another visualizer with key '" + key + "' already exists.");
		}
		PathVisualizer<?> visualizer = PathPlugin.getInstance().getDatabase().newPathVisualizer(key, nameFormat,
				particle, displayItem, particleDistance, particleSteps, schedulerPeriod, 3);
		pathVisualizerMap.put(visualizer);
		return visualizer;
	}

	public boolean deletePathVisualizer(PathVisualizer<?> visualizer) {
		PathPlugin.getInstance().getDatabase().deletePathVisualizer(visualizer);
		return pathVisualizerMap.remove(visualizer.getKey()) != null;
	}

	public Stream<PathVisualizer<?>> getPathVisualizerStream() {
		return pathVisualizerMap.values().stream();
	}
}
