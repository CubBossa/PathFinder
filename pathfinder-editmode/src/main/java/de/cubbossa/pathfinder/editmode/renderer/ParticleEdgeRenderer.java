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
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.utils.ReflectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    editModeTasks = ConcurrentHashMap.newKeySet();
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

    for (Node node : nodes) {
      edges.remove(node.getNodeId());
    }
    rendered.addAll(nodes.stream().map(Node::getNodeId).toList());
    Collection<CompletableFuture<Void>> futures = new HashSet<>();

    // all edges from rendered nodes to adjacent nodes
    Collection<Edge> toRender = nodes.stream()
        .map(Node::getEdges).flatMap(Collection::stream)
        .collect(Collectors.toSet());
    // all edges from adjacent nodes to rendered nodes
    Storage storage = PathFinderProvider.get().getStorage();
    return storage.loadEdgesTo(nodes.stream().map(Node::getNodeId).collect(Collectors.toSet())).thenCompose(uuidCollectionMap -> {
      toRender.addAll(uuidCollectionMap.values().stream()
          .flatMap(Collection::stream)
          .filter(edge -> rendered.contains(edge.getStart()))
          .toList());

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
    });
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    Collection<UUID> rendered = new HashSet<>(this.rendered);
    clear(player);
    rendered.removeAll(nodes.stream().map(Node::getNodeId).toList());
    return pathFinder.getStorage().loadNodes(rendered).thenCompose(n -> renderNodes(player, n));
  }

  private void updateRenderer(PathPlayer<Player> player) {
    CompletableFuture.runAsync(() -> {

      var sched = Bukkit.getScheduler();
      new HashSet<>(editModeTasks).forEach(sched::cancelTask);

      Set<ParticleInfo> packets = ConcurrentHashMap.newKeySet();
      packets.addAll(generateLocations(player));

      Function<Integer, Supplier<Collection<ParticleInfo>>> packetSupplier = i -> () -> {
        Player p = player.unwrap();
        if (p == null || !p.isOnline()) {
          throw new IllegalStateException("Trying to render edit mode packets for offline player.");
        }
        List<ParticleInfo> packet = new ArrayList<>();
        double distSquared = Math.pow(config.getEdgeParticleRenderDistance(), 2);
        packets.forEach((info) -> {
          if (!Objects.equals(info.location().getWorld(), p.getWorld())) {
            return;
          }
          if (info.location().distanceSquared(p.getLocation()) > distSquared) {
            return;
          }
          packet.add(info);
        });
        return CollectionUtils.everyNth(packet, 2, i);
      };
      editModeTasks.add(Bukkit.getScheduler().runTaskTimerAsynchronously(ReflectionUtils.getPlugin(), () -> {
        packets.clear();
        packets.addAll(generateLocations(player));
      }, config.getEdgeParticleTickDelay() * 5L, config.getEdgeParticleTickDelay() * 5L).getTaskId());

      editModeTasks.add(startTask(packetSupplier.apply(0), player, 0));
      editModeTasks.add(startTask(packetSupplier.apply(1), player, config.getEdgeParticleTickDelay() / 2));
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  private Collection<ParticleInfo> generateLocations(PathPlayer<Player> player) {
    Map<UUID, Collection<UUID>> included = new HashMap<>();
    Set<ParticleInfo> packets = new HashSet<>();
    for (var edge : edges.flatValues()) {
      if (!Objects.equals(edge.getStart().getWorld(), edge.getEnd().getWorld())) {
        continue;
      }
      boolean directed = edge.isDirected();
      if (!directed && included.computeIfAbsent(edge.endId, x -> new HashSet<>()).contains(edge.startId)) {
        continue;
      }
      included.computeIfAbsent(edge.startId, x -> new HashSet<>()).add(edge.endId);

      Vector a = BukkitVectorUtils.toBukkit(edge.getStart().asVector());
      Vector b = BukkitVectorUtils.toBukkit(edge.getEnd().asVector());
      double dist = a.distance(b);


      Vector lastLoc = a;

      for (float i = 0; i < dist; i += config.getEdgeParticleSpacing() + config.getEdgeParticleSpacing() * 10 * lastLoc.distance(player.unwrap().getLocation().toVector()) / config.getEdgeParticleRenderDistance()) {
        java.awt.Color c = directed
            ? LerpUtils.lerp(config.getEdgeParticleColorFrom(), config.getEdgeParticleColorTo(), i / dist)
            : config.getEdgeParticleColorFrom();

        org.bukkit.Location loc = BukkitUtils.lerp(a, b, i / dist)
            .toLocation(Bukkit.getWorld(edge.getStart().getWorld().getUniqueId()));
        lastLoc = loc.toVector();
        // TODO conversion can be optimized
        packets.add(new ParticleInfo(loc, Color.fromRGB(c.getRGB() & 0xffffff)));
      }
    }
    return packets;
  }

  private int startTask(Supplier<Collection<ParticleInfo>> packets, PathPlayer<Player> player, int delay) {
    return Bukkit.getScheduler().runTaskTimerAsynchronously(ReflectionUtils.getPlugin(), () -> {
      Player p = player.unwrap();
      for (ParticleInfo c : packets.get()) {
        p.spawnParticle(Particle.REDSTONE, c.location(), 1, new Particle.DustOptions(c.color(), 1));
      }
    }, delay, config.getEdgeParticleTickDelay()).getTaskId();
  }

  private record ParticleInfo(org.bukkit.Location location, Color color) {
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
