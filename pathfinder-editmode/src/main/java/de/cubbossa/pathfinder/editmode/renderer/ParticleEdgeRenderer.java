package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathfinder.util.FutureUtils;
import de.cubbossa.pathfinder.util.LerpUtils;
import de.cubbossa.pathfinder.util.MultiMap;
import de.cubbossa.pathfinder.util.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.utils.ParticleUtils;
import xyz.xenondevs.particle.utils.ReflectionUtils;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@Setter
public class ParticleEdgeRenderer implements GraphRenderer<Player> {

    private final PathFinder pathFinder;

    private final Collection<UUID> rendered;
    private final MultiMap<UUID, UUID, ParticleEdge> edges;
    private Collection<Integer> editModeTasks;
    private Color colorFrom = new Color(255, 0, 0);
    private Color colorTo = new Color(0, 127, 255);
    private float particleDistance = .3f;
    private int tickDelay = 6;

    public ParticleEdgeRenderer() {
        pathFinder = PathFinderProvider.get();
        rendered = new HashSet<>();
        edges = new MultiMap<>();
        editModeTasks = new HashSet<>();
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    var sched = Bukkit.getScheduler();
    editModeTasks.forEach(sched::cancelTask);
    rendered.clear();
    edges.clear();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {

      rendered.addAll(nodes.stream().map(Node::getNodeId).toList());
      Collection<CompletableFuture<Void>> futures = new HashSet<>();

      // all edges from rendered nodes to adjacent nodes
      Collection<Edge> toRender = nodes.stream()
              .map(Node::getEdges).flatMap(Collection::stream)
              .collect(Collectors.toSet());
      // all edges from adjacent nodes to rendered nodes
      Storage storage = PathFinderProvider.get().getStorage();
      toRender.addAll(storage.loadEdgesTo(nodes).join());

      for (Edge edge : toRender) {
          FutureUtils.both(edge.resolveStart(), edge.resolveEnd()).thenAccept(entry -> {
              Node startNode = entry.getKey();
              Node endNode = entry.getValue();

              ParticleEdge particleEdge = new ParticleEdge(startNode.getNodeId(), endNode.getNodeId(), startNode.getLocation(), endNode.getLocation(), true);
              ParticleEdge present = edges.get(edge.getEnd(), edge.getStart());
              if (present != null) {
                  present.start = endNode.getLocation();
                  present.end = startNode.getLocation();
                  particleEdge.setDirected(false);
                  present.setDirected(false);
              }
              edges.put(edge.getStart(), edge.getEnd(), particleEdge);
          });
      }
      return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
              .thenRun(() -> updateRenderer(player));
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
      Collection<UUID> rendered = new HashSet<>(this.rendered);
      clear(player);
      rendered.removeAll(nodes.stream().map(Node::getNodeId).toList());
      return renderNodes(player, pathFinder.getStorage().loadNodes(rendered).join());
  }

  private void updateRenderer(PathPlayer<Player> player) {
    CompletableFuture.runAsync(() -> {

        Map<UUID, Collection<UUID>> included = new HashMap<>();

        var sched = Bukkit.getScheduler();
        editModeTasks.forEach(sched::cancelTask);

        List<Object> packets = new ArrayList<>();
        Map<Color, ParticleBuilder> particles = new HashMap<>();

        for (var edge : edges.flatValues()) {
            if (!Objects.equals(edge.getStart().getWorld(), edge.getEnd().getWorld())) {
                return;
            }
            boolean directed = edge.isDirected();
            if (!directed && included.computeIfAbsent(edge.endId, x -> new HashSet<>()).contains(edge.startId)) {
                continue;
            }
            included.computeIfAbsent(edge.startId, x -> new HashSet<>()).add(edge.endId);

            Vector a = VectorUtils.toBukkit(edge.getStart().asVector());
            Vector b = VectorUtils.toBukkit(edge.getEnd().asVector());
            double dist = a.distance(b);

            for (float i = 0; i < dist; i += particleDistance) {
                Color c = directed ? LerpUtils.lerp(colorFrom, colorTo, i / dist) : colorFrom;

                ParticleBuilder builder = particles.computeIfAbsent(c,
                        k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
                packets.add(builder.setLocation(LerpUtils.lerp(a, b, i / dist)
                                .toLocation(Bukkit.getWorld(edge.getStart().getWorld().getUniqueId())))
                        .toPacket());
            }
        }
        editModeTasks.add(startTask(everyNth(packets, 2, 0), player.unwrap(), 0));
        editModeTasks.add(startTask(everyNth(packets, 2, 1), player.unwrap(), tickDelay / 2));
    }).exceptionally(throwable -> {
        throwable.printStackTrace();
        return null;
    });
  }

    private int startTask(List<Object> packets, Player player, int delay) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(ReflectionUtils.getPlugin(), () -> {
            ParticleUtils.sendBulk(packets, player);
        }, delay, tickDelay).getTaskId();
    }

    private <E> List<E> everyNth(List<E> in, int n, int offset) {
        List<E> result = new ArrayList<>();
        for (int i = offset % n; i < in.size(); i += n) {
            result.add(in.get(i));
        }
        return result;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    private static class ParticleEdge {
        private final UUID startId;
        private final UUID endId;
        private Location start;
        private Location end;
        private boolean directed = true;
    }
}
