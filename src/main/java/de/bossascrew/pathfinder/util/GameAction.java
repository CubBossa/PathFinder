package de.bossascrew.pathfinder.util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class GameAction<V> {

    private final Collection<Consumer<V>> subscribers;

    public GameAction() {
        subscribers = new HashSet<>();
    }

    public void inform(@Nullable V value) {
        for(Consumer<V> consumer : subscribers) {
            consumer.accept(value);
        }
    }

    public void subscribe(Consumer<V> subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Consumer<V> subscriber) {
        subscribers.remove(subscriber);
    }
}
