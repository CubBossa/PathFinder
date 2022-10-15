package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;

import java.util.Map;

public class ScriptLineParticleVisualizerType extends BezierVisualizerType<ScriptLineParticleVisualizer> {

	public ScriptLineParticleVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public ScriptLineParticleVisualizer create(NamespacedKey key, String nameFormat) {
		return new ScriptLineParticleVisualizer(key, nameFormat);
	}

	@Override
	public Message getInfoMessage(ScriptLineParticleVisualizer element) {
		return Messages.CMD_ADV_VIS_INFO_PARTICLES.format(TagResolver.builder()
				.resolver(Formatter.number("point-distance", element.getPointDistance()))
				.resolver(Placeholder.parsed("particle", element.getParticleFunction()))
				.resolver(Placeholder.parsed("particle-data", element.getParticleDataFunction()))
				.resolver(Placeholder.parsed("speed", element.getParticleDataFunction()))
				.resolver(Placeholder.parsed("amount", element.getAmountFunction()))
				.resolver(Placeholder.parsed("offset-x", element.getParticleOffsetXFunction()))
				.resolver(Placeholder.parsed("offset-y", element.getParticleOffsetYFunction()))
				.resolver(Placeholder.parsed("offset-z", element.getParticleOffsetZFunction()))
				.resolver(Placeholder.parsed("path-x", element.getPathOffsetXFunction()))
				.resolver(Placeholder.parsed("path-y", element.getPathOffsetYFunction()))
				.resolver(Placeholder.parsed("path-z", element.getPathOffsetZFunction()))
				.build());
	}

	@Override
	public String getCommandName() {
		return "advanced-particle";
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
				.then(new LiteralArgument("particle")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setParticleFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("particle-data")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setParticleDataFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("amount")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setAmountFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("speed")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setSpeedFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("offset-x")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setParticleOffsetXFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("offset-y")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setParticleOffsetYFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("offset-z")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setParticleOffsetZFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("path-x")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setPathOffsetXFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("path-y")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setPathOffsetYFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("path-z")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((ScriptLineParticleVisualizer) objects[0]).setPathOffsetZFunction((String) objects[1]);
								})));
	}

	@Override
	public Map<String, Object> serialize(ScriptLineParticleVisualizer visualizer) {
		super.serialize(visualizer);
		return new LinkedHashMapBuilder<String, Object>()
				.put("particle-steps", visualizer.getSchedulerSteps())
				.put("particle", visualizer.getParticleFunction())
				.put("particle-data", visualizer.getParticleDataFunction())
				.put("speed", visualizer.getSpeedFunction())
				.put("amount", visualizer.getAmountFunction())
				.put("offset-x", visualizer.getParticleOffsetXFunction())
				.put("offset-y", visualizer.getParticleOffsetZFunction())
				.put("offset-z", visualizer.getParticleOffsetZFunction())
				.put("path-x", visualizer.getPathOffsetZFunction())
				.put("path-y", visualizer.getPathOffsetZFunction())
				.put("path-z", visualizer.getPathOffsetZFunction())
				.put("sample-rate", visualizer.getBezierSamplingRate())
				.put("point-distance", visualizer.getPointDistance())
				.build();
	}

	@Override
	public void deserialize(ScriptLineParticleVisualizer visualizer, Map<String, Object> values) {
		super.deserialize(visualizer, values);
		if (values.containsKey("particle-steps")) {
			visualizer.setSchedulerSteps((Integer) values.get("particle-steps"));
		}
		if (values.containsKey("particle")) {
			visualizer.setParticleFunction((String) values.get("particle"));
		}
		if (values.containsKey("particle-data")) {
			visualizer.setParticleDataFunction((String) values.get("particle-data"));
		}
		if (values.containsKey("speed")) {
			visualizer.setSpeedFunction((String) values.get("speed"));
		}
		if (values.containsKey("amount")) {
			visualizer.setAmountFunction((String) values.get("amount"));
		}
		if (values.containsKey("offset-x")) {
			visualizer.setParticleOffsetXFunction((String) values.get("offset-x"));
		}
		if (values.containsKey("offset-y")) {
			visualizer.setParticleOffsetXFunction((String) values.get("offset-y"));
		}
		if (values.containsKey("offset-z")) {
			visualizer.setParticleOffsetXFunction((String) values.get("offset-z"));
		}
		if (values.containsKey("path-x")) {
			visualizer.setParticleOffsetXFunction((String) values.get("path-x"));
		}
		if (values.containsKey("path-y")) {
			visualizer.setParticleOffsetXFunction((String) values.get("path-y"));
		}
		if (values.containsKey("path-z")) {
			visualizer.setParticleOffsetXFunction((String) values.get("path-z"));
		}
		if (values.containsKey("sample-rate")) {
			visualizer.setBezierSamplingRate((Integer) values.get("sample-rate"));
		}
		if (values.containsKey("point-distance")) {
			visualizer.setPointDistance(((Double) values.get("point-distance")).floatValue());
		}
	}
}
