package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Named;
import de.cubbossa.pathapi.storage.NodeStorageImplementation;

public interface NodeType<N extends Node> extends Keyed, NodeStorageImplementation<N> {
}
