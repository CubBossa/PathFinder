package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.events.ParticleVisualizerStepsChangedEvent;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.wrappers.ParticleData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ParticleVisualizerType extends BezierVisualizerType<ParticleVisualizer> {

	public ParticleVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public ParticleVisualizer create(NamespacedKey key, String nameFormat) {
		return new ParticleVisualizer(key, nameFormat);
	}

	@Override
	public Message getInfoMessage(ParticleVisualizer element) {
		return Messages.CMD_VIS_INFO_PARTICLES.format(TagResolver.builder()
				.resolver(Placeholder.component("particle", Messages.formatParticle(element.getParticle(), element.getParticleData())))
				.resolver(Placeholder.component("particle-steps", Component.text(element.getSchedulerSteps())))
				.resolver(Placeholder.component("amount", Component.text(element.getAmount())))
				.resolver(Placeholder.component("speed", Component.text(element.getSpeed())))
				.resolver(Placeholder.component("offset", Messages.formatVector(element.getOffset())))
				.resolver(Formatter.number("particle-distance", element.getPointDistance()))
				.build());
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
				.then(new LiteralArgument("particle")
						.withPermission(PathPlugin.PERM_CMD_PV_PARTICLES)
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
						.withPermission(PathPlugin.PERM_CMD_PV_PARTICLE_STEPS)
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

	@Override
	public Map<String, Object> serialize(ParticleVisualizer visualizer) {
		super.serialize(visualizer);
		return new LinkedHashMapBuilder<String, Object>()
				.put("particle-steps", visualizer.getSchedulerSteps())
				.put("particle", visualizer.getParticle().toString())
				.put("particle-data", visualizer.getParticleData())
				.put("speed", visualizer.getSpeed())
				.put("amount", visualizer.getAmount())
				.put("offset", visualizer.getOffset())
				.put("sample-rate", visualizer.getBezierSamplingRate())
				.put("point-distance", visualizer.getPointDistance())
				.build();
	}

	@Override
	public void deserialize(ParticleVisualizer visualizer, Map<String, Object> values) {
		super.deserialize(visualizer, values);
		if (values.containsKey("particle-steps")) {
			visualizer.setSchedulerSteps((Integer) values.get("particle-steps"));
		}
		if (values.containsKey("particle")) {
			visualizer.setParticle(Particle.valueOf((String) values.get("particle")));
		}
		if (values.containsKey("particle-data")) {
			visualizer.setParticleData(values.get("particle-data"));
		}
		if (values.containsKey("speed")) {
			visualizer.setSpeed(((Double) values.get("speed")).floatValue());
		}
		if (values.containsKey("amount")) {
			visualizer.setAmount((Integer) values.get("amount"));
		}
		if (values.containsKey("offset")) {
			visualizer.setOffset((Vector) values.get("offset"));
		}
		if (values.containsKey("sample-rate")) {
			visualizer.setBezierSamplingRate((Integer) values.get("sample-rate"));
		}
		if (values.containsKey("point-distance")) {
			visualizer.setPointDistance(((Double) values.get("point-distance")).floatValue());
		}
	}
}
