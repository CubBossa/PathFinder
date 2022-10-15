package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;

public class SerializableAdvancedParticleVisualizerType extends BezierVisualizerType<SerializableAdvancedParticleVisualizer> {

	public SerializableAdvancedParticleVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public SerializableAdvancedParticleVisualizer create(NamespacedKey key, String nameFormat) {
		return new SerializableAdvancedParticleVisualizer(key, nameFormat);
	}

	@Override
	public Message getInfoMessage(SerializableAdvancedParticleVisualizer element) {
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
									((SerializableAdvancedParticleVisualizer) objects[0]).setParticleFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("particle-data")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setParticleDataFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("amount")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setAmountFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("speed")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setSpeedFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("offset-x")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setParticleOffsetXFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("offset-y")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setParticleOffsetYFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("offset-z")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setParticleOffsetZFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("path-x")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setPathOffsetXFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("path-y")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setPathOffsetYFunction((String) objects[1]);
								})))
				.then(new LiteralArgument("path-z")
						.then(new TextArgument("java-script")
								.executes((commandSender, objects) -> {
									((SerializableAdvancedParticleVisualizer) objects[0]).setPathOffsetZFunction((String) objects[1]);
								})));
	}
}
