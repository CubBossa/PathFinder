package de.cubbossa.pathfinder.util;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FutureUtils {

  public static <A, B> CompletableFuture<Map.Entry<A, B>> both(CompletableFuture<A> a, CompletableFuture<B> b) {
    return CompletableFuture.allOf(a, b).thenApply((u) -> {
      try {
        return Map.entry(a.get(), b.get());
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
