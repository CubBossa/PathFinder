package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathfinder.PathFinderConf;
import de.cubbossa.pathfinder.util.*;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Setter
public class ParticleEdgeRenderer implements GraphRenderer<Player> {

  private final PathFinder pathFinder;

  private final Collection<UUID> rendered;
  private final MultiMap<UUID, UUID, ParticleEdge> edges;
  private final Collection<Integer> editModeTasks;
  private final PathFinderConf.EditModeConfig config;

  public ParticleEdgeRenderer() {
    this(new PathFinderConf.EditModeConf());
  }

  public ParticleEdgeRenderer(PathFinderConfig.EditModeConfig editModeConfig) {
    pathFinder = PathFinderProvider.get();
    rendered = new HashSet<>();
    edges = new MultiMap<>();
    editModeTasks = new HashSet<>();
    config = editModeConfig;
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
      var future = FutureUtils.both(edge.resolveStart(), edge.resolveEnd()).thenAccept(entry -> {
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
      futures.add(future);
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

      Map<org.bukkit.Location, Object> packets = new HashMap<>();
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

        for (float i = 0; i < dist; i += config.getEdgeParticleSpacing()) {
          Color c = directed
              ? LerpUtils.lerp(config.getEdgeParticleColorFrom(), config.getEdgeParticleColorTo(), i / dist)
              : config.getEdgeParticleColorFrom();

          ParticleBuilder builder = particles.computeIfAbsent(c, k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
          org.bukkit.Location loc = BukkitUtils.lerp(a, b, i / dist)
              .toLocation(Bukkit.getWorld(edge.getStart().getWorld().getUniqueId()));
          packets.put(loc, builder.setLocation(loc).toPacket());
        }
      }
      Function<Integer, Supplier<List<Object>>> packetSupplier = i -> () -> {
        Player p = player.unwrap();
        if (p == null || !p.isOnline()) {
          throw new IllegalStateException("Trying to render edit mode packets for offline player.");
        }
        List<Object> packet = new ArrayList<>();
        double distSquared = Math.pow(config.getEdgeParticleRenderDistance(), 2);
        packets.forEach((location, o) -> {
          if (!Objects.equals(location.getWorld(), p.getWorld())) {
            return;
          }
          if (location.distanceSquared(p.getLocation()) > distSquared) {
            return;
          }
          packet.add(o);
        });
        return CollectionUtils.everyNth(packet, 2, i);
      };

      editModeTasks.add(startTask(packetSupplier.apply(0), player.unwrap(), 0));
      editModeTasks.add(startTask(packetSupplier.apply(1), player.unwrap(), config.getEdgeParticleTickDelay() / 2));
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  private int startTask(Supplier<List<Object>> packets, Player player, int delay) {
    return Bukkit.getScheduler().runTaskTimerAsynchronously(ReflectionUtils.getPlugin(), () -> {
      ParticleUtils.sendBulk(packets.get(), player);
    }, delay, config.getEdgeParticleTickDelay()).getTaskId();
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
