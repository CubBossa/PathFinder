package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.wrappers.ParticleData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
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
				.resolver(Formatter.number("speed", element.getSpeed()))
				.resolver(Placeholder.component("offset", Messages.formatVector(element.getOffset())))
				.resolver(Formatter.number("point-distance", element.getPointDistance()))
				.build());
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
				.then(new LiteralArgument("particle")
						.then(new ParticleArgument("particle")
								.executes((commandSender, objects) -> {
									ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
									onSetParticle(commandSender, visualizer, (ParticleData) objects[argumentOffset], null, null, null);
								})
								.then(new IntegerArgument("amount", 1)
										.executes((commandSender, objects) -> {
											ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
											onSetParticle(commandSender, visualizer, (ParticleData) objects[argumentOffset], (Integer) objects[argumentOffset + 1], null, null);
										})
										.then(new FloatArgument("speed", 0)
												.executes((commandSender, objects) -> {
													ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
													onSetParticle(commandSender, visualizer, (ParticleData) objects[argumentOffset], (Integer) objects[argumentOffset + 1], (Float) objects[argumentOffset + 2], null);
												})
												.then(new LocationArgument("offset")
														.executes((commandSender, objects) -> {
															ParticleVisualizer visualizer = (ParticleVisualizer) objects[visualizerIndex];
															onSetParticle(commandSender, visualizer, (ParticleData) objects[argumentOffset], (Integer) objects[argumentOffset + 1], (Float) objects[argumentOffset + 2], ((Location) objects[argumentOffset + 3]).toVector());
														})
												)
										)
								)
						))
				.then(subCommand("particle-steps", new IntegerArgument("amount", 1), ParticleVisualizer.PROP_SCHEDULER_STEPS));
	}

	private <T> void onSetParticle(CommandSender sender, ParticleVisualizer visualizer, ParticleData<T> particle, @Nullable Integer amount, @Nullable Float speed, @Nullable Vector offset) {
		VisualizerHandler.getInstance().setProperty(sender, visualizer, particle.particle(), "particle", true, visualizer::getParticle, visualizer::setParticle);
		VisualizerHandler.getInstance().setProperty(sender, visualizer, particle.data(), "particle-data", true, visualizer::getParticleData, visualizer::setParticleData);
		if (amount != null) {
			VisualizerHandler.getInstance().setProperty(sender, visualizer, amount, "amount", true, visualizer::getAmount, visualizer::setAmount);
		}
		if (speed != null) {
			VisualizerHandler.getInstance().setProperty(sender, visualizer, speed, "speed", true, visualizer::getSpeed, visualizer::setSpeed);
		}
		if (offset != null) {
			VisualizerHandler.getInstance().setProperty(sender, visualizer, offset, "speed", true, visualizer::getOffset, visualizer::setOffset);
		}
	}

	@Override
	public Map<String, Object> serialize(ParticleVisualizer visualizer) {
		super.serialize(visualizer);
		return new LinkedHashMapBuilder<String, Object>()
				.put(ParticleVisualizer.PROP_SCHEDULER_STEPS.getKey(), visualizer.getSchedulerSteps())
				.put("interval", visualizer.getInterval())
				.put("particle", visualizer.getParticle().toString())
				.put("particle-data", YamlUtils.wrap(visualizer.getParticleData()))
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
		loadProperty(values, visualizer, ParticleVisualizer.PROP_SCHEDULER_STEPS);
		if (values.containsKey("interval")) {
			visualizer.setInterval((Integer) values.get("interval"));
		}
		if (values.containsKey("particle")) {
			visualizer.setParticle(Particle.valueOf((String) values.get("particle")));
		}
		if (values.containsKey("particle-data")) {
			visualizer.setParticleData(YamlUtils.unwrap(values.get("particle-data")));
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
