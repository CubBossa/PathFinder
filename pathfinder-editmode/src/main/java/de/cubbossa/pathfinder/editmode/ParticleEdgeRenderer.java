package de.cubbossa.pathfinder.editmode;

import de.cubbossa.pathfinder.api.misc.Location;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.util.LerpUtils;
import de.cubbossa.pathfinder.util.VectorUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

@Getter
@Setter
public class ParticleEdgeRenderer implements GraphRenderer<Player> {

  @Setter
  @Getter
  @RequiredArgsConstructor
  private static class ParticleEdge {
    private final UUID startId;
    private final UUID endId;
    private final Location start;
    private final Location end;
    private boolean directed = true;
  }

  private Color colorFrom = new Color(255, 0, 0);
  private Color colorTo = new Color(0, 127, 255);

  private float particleDistance = .3f;
  private int tickDelay = 5;

  private final Collection<ParticleEdge> edges;
  private final Collection<Integer> editModeTasks;

  public ParticleEdgeRenderer() {
    edges = new HashSet<>();
    editModeTasks = new HashSet<>();
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    var sched = Bukkit.getScheduler();
    editModeTasks.forEach(sched::cancelTask);
    edges.clear();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {

    Collection<CompletableFuture<Void>> futures = new HashSet<>();
    for (Node<?> node : nodes) {
      for (Edge edge : node.getEdges()) {
        Optional<ParticleEdge> contained = edges.stream()
            .filter(
                e -> e.getStartId().equals(edge.getEnd()) && e.getEndId().equals(edge.getStart()))
            .findAny();
        contained.ifPresentOrElse(e -> {
          e.setDirected(false);
          edges.remove(e);
        }, () -> {
          futures.add(edge.resolveStart().thenAccept(startNode -> {
            Node<?> endNode = edge.resolveEnd().join();
            edges.add(new ParticleEdge(
                edge.getStart(), edge.getEnd(),
                startNode.getLocation(), endNode.getLocation()
            ));
          }));
        });
      }
    }
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenRun(() -> updateRenderer(player));
  }

  private void updateRenderer(PathPlayer<Player> player) {
    CompletableFuture.runAsync(() -> {

      var sched = Bukkit.getScheduler();
      new ArrayList<>(editModeTasks).forEach(sched::cancelTask);

      Map<Color, List<Object>> packets = new HashMap<>();
      Map<Color, ParticleBuilder> particles = new HashMap<>();

      for (var edge : edges) {
        if (!Objects.equals(edge.getStart().getWorld(), edge.getEnd().getWorld())) {
          return;
        }
        boolean directed = edge.isDirected();

        Vector a = VectorUtils.toBukkit(edge.getStart().asVector());
        Vector b = VectorUtils.toBukkit(edge.getEnd().asVector());
        double dist = a.distance(b);

        for (float i = 0; i < dist; i += particleDistance) {
          Color c = directed ? LerpUtils.lerp(colorFrom, colorTo, i / dist) : colorFrom;

          ParticleBuilder builder = particles.computeIfAbsent(c,
              k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
          packets.computeIfAbsent(c, x -> new ArrayList<>()).add(builder.setLocation(
                  LerpUtils.lerp(a, b, i / dist)
                      .toLocation(Bukkit.getWorld(edge.getStart().getWorld().getUniqueId())))
              .toPacket());
        }
      }
      for (var entry : packets.entrySet()) {
        editModeTasks.add(
            TaskManager.startSingularTask(entry.getValue(), tickDelay, player.unwrap()));
      }
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }
}
