package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;

public class AdvancedParticleVisualizerType extends BezierVisualizerType<AdvancedParticleVisualizer> {

    public AdvancedParticleVisualizerType(NamespacedKey key) {
        super(key);
    }

    @Override
    public AdvancedParticleVisualizer create(NamespacedKey key, String nameFormat) {
        return new SerializableAdvancedParticleVisualizer(key, nameFormat);
    }

    @Override
    public Message getInfoMessage(AdvancedParticleVisualizer element) {
        return new Message("lol");
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
