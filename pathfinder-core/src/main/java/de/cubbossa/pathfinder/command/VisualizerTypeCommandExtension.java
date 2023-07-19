package de.cubbossa.pathfinder.command;

import dev.jorel.commandapi.arguments.Argument;

public interface VisualizerTypeCommandExtension {

    Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset);
}
