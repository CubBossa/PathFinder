package de.cubbossa.pathfinder.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class FutureUtils {

    public static <A, B> CompletableFuture<Map.Entry<A, B>> both(CompletableFuture<A> a, CompletableFuture<B> b) {
        CompletableFuture<Map.Entry<A, B>> result = new CompletableFuture<>();

        AtomicReference<A> aRef = new AtomicReference<>();
        AtomicReference<B> bRef = new AtomicReference<>();

        a.thenAccept(a1 -> {
            if (bRef.get() != null) {
                result.complete(new AbstractMap.SimpleEntry<>(a1, bRef.get()));
                return;
            }
            aRef.set(a1);
        });

        b.thenAccept(b1 -> {
            if (aRef.get() != null) {
                result.complete(new AbstractMap.SimpleEntry<>(aRef.get(), b1));
                return;
            }
            bRef.set(b1);
        });
        return result;
    }
}
