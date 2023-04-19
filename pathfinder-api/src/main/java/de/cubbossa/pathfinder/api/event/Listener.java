package de.cubbossa.pathfinder.api.event;

import java.util.UUID;
import java.util.function.Consumer;

public record Listener<E extends PathFinderEvent>(UUID id, Class<E> eventType, Consumer<? super E> handler) {
}
