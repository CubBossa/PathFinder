package de.cubbossa.pathfinder.event;

import de.cubbossa.disposables.Disposable;
import java.util.UUID;
import java.util.function.Consumer;

public record Listener<E extends PathFinderEvent>(UUID id, Class<E> eventType,
                                                  Consumer<? super E> handler) implements Disposable {
}
