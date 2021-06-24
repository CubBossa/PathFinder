package de.bossascrew.pathfinder.util;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SubscribtionHandler<K, V> {

    private final Map<K, Consumer<V>> subscribers;

    public SubscribtionHandler() {
        subscribers = new HashMap<>();
    }

    public void perform() {
        perform(null);
    }

    public void perform(@Nullable V value) {
        for(Consumer<V> consumer : subscribers.values()) {
            consumer.accept(value);
        }
    }

    public void subscribe(K key, Consumer<V> subscriber) {
        subscribers.put(key, subscriber);
    }

    public boolean unsubscribe(K key) {
        return subscribers.remove(key) != null;
    }
}
