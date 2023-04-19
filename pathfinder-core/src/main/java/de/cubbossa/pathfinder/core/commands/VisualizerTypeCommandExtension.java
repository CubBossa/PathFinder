package de.cubbossa.pathfinder.core.commands;

import dev.jorel.commandapi.ArgumentTree;

public interface VisualizerTypeCommandExtension {

  ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset);
}
