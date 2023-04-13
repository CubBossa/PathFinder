package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public class EdgeCache {

  @RequiredArgsConstructor
  @EqualsAndHashCode
  private static class Key {
    private final UUID start;
    private final UUID end;
  }

  public final Cache<Key, SimpleEdge> cache;

  public EdgeCache() {
    cache = Caffeine.newBuilder()
        .build();
  }

  public Optional<SimpleEdge> getEdge(UUID from, UUID to, Supplier<Optional<SimpleEdge>> loader) {
    return Optional.ofNullable(
        cache.get(new Key(from, to), key -> loader.get().orElse(null))
    );
  }

  public Collection<SimpleEdge> getEdgesFrom(UUID from) {
    Collection<SimpleEdge> result = new HashSet<>();
    cache.asMap().forEach((key, value) -> {
      if (key.start.equals(from)) {
        result.add(value);
      }
    });
    return result;
  }

  public Collection<SimpleEdge> getEdgesTo(UUID to) {
    Collection<SimpleEdge> result = new HashSet<>();
    cache.asMap().forEach((key, value) -> {
      if (key.end.equals(to)) {
        result.add(value);
      }
    });
    return result;
  }

  public void write(SimpleEdge edge) {
    cache.put(new Key(edge.getStart(), edge.getEnd()), edge);
  }

  public void invalidate(SimpleEdge edge) {
    cache.invalidate(new Key(edge.getStart(), edge.getEnd()));
  }

  public void invalidate(UUID node) {
    cache.invalidateAll(cache.asMap().values().stream()
        .filter(edge -> edge.getStart().equals(node))
        .filter(edge -> edge.getEnd().equals(node))
        .map(edge -> new Key(edge.getStart(), edge.getEnd()))
        .toList());
  }


}
