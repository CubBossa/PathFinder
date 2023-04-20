package de.cubbossa.pathfinder.commands;

import dev.jorel.commandapi.ArgumentTree;

public interface VisualizerTypeCommandExtension {

  ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset);
}
