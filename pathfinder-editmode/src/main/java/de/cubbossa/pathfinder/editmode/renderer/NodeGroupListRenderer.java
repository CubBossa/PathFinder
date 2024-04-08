package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.cliententities.PlayerSpace;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.editor.GraphRenderer;
import de.cubbossa.pathfinder.event.NodeGroupSaveEvent;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NodeGroupListRenderer implements Listener, GraphRenderer<Player> {

  @Getter
  @Setter
  private static final class Context {
    private final PlayerSpace playerSpace;
    private final Collection<Node> rendered;
    private final Map<UUID, NodeContext> displayed;

    public Context(UUID player) {
        playerSpace = PlayerSpace.builder().withPlayer(player).build();
        rendered = new HashSet<>();
      displayed = new ConcurrentHashMap<>();
    }

    private record NodeContext(Node node, TextDisplay display) {
    }
  }

  private final Plugin plugin;

  private final Map<UUID, Context> contextMap;

  private boolean hasHeldGroupToolsBefore = false;
  private final long cooldown = 100;
  private long lastCheck = 0;
  private final int animationTickDuration = 4;

  private final double angleDot;
  private final double distanceSquared;

  private final de.cubbossa.pathfinder.event.Listener<?> groupChangeListener;

  public NodeGroupListRenderer(Plugin plugin, double angle, double distance) {
    this.plugin = plugin;
    contextMap = new HashMap<>();

    angleDot = Math.cos(angle * Math.PI / 180);
    distanceSquared = Math.pow(distance, 2);

    groupChangeListener = PathFinderProvider.get().getEventDispatcher().listen(NodeGroupSaveEvent.class, e -> {
      CompletableFuture.runAsync(() -> {
        contextMap.forEach((uuid, context) -> {
          Player player = Bukkit.getPlayer(uuid);
          if (player == null) {
            return;
          }
          context.displayed.values().forEach(nodeContext -> {
            showText(nodeContext.node, player);
          });
        });
      });
    });

    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public void dispose() {
    contextMap.values().forEach(context -> {
      try {
        context.playerSpace.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    PathFinderProvider.get().getEventDispatcher().drop(groupChangeListener);
    PlayerMoveEvent.getHandlerList().unregister(this);
  }

  private Context context(Player player) {
    return contextMap.computeIfAbsent(player.getUniqueId(), Context::new);
  }

  /**
   * Lets check the following:
   * - if the cooldown of x ms is over.
   * - if the player holds group tools
   * - filter nodes that are not displayed
   * - if any of the rendered nodes matches the location criteria
   * - if the node has any groups
   * if all fulfilled ->
   */
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    long now = System.currentTimeMillis();
    // lets do performance a favor
    if (now - lastCheck < cooldown) {
      return;
    }
    lastCheck = now;

    Player player = event.getPlayer();
    if (!holdsGroupTools(player)) {
      // Hide all currently visible texts.
      if (hasHeldGroupToolsBefore) {
        new HashMap<>(context(player).displayed).forEach((k, v) -> hideText(v.node(), player));
      }
      return;
    }
    hasHeldGroupToolsBefore = true;
    CompletableFuture.runAsync(() -> {
      for (Node node : context(player).rendered) {
        evaluate(node, player);
      }
    });
  }

  private void evaluate(Node node, Player player) {
    Location nodeLoc = BukkitVectorUtils.toBukkit(node.getLocation());
    if (!Objects.equals(nodeLoc.getWorld(), player.getLocation().getWorld())) {
      hideText(node, player);
      return;
    }
    if (nodeLoc.distanceSquared(player.getLocation()) > distanceSquared) {
      hideText(node, player);
      return;
    }

    // dot product divided by length = angle. positive -> viewing in same direction, the smaller the closer
    Vector view = player.getLocation().getDirection().normalize();
    Vector toNode = nodeLoc.clone().subtract(player.getEyeLocation().toVector()).toVector().normalize();
    double dot = view.dot(toNode);
    if (dot / (view.length() * toNode.length()) < angleDot) {
      hideText(node, player);
      return;
    }
    showText(node, player);
  }

  private CompletableFuture<Void> showText(Node node, Player player) {
    Context ctx = context(player);

    if (ctx.displayed.containsKey(node.getNodeId())) {
      return CompletableFuture.completedFuture(null);
    }

    return PathFinderProvider.get().getStorage().loadGroupsOfNodes(Collections.singleton(node)).thenAccept(nodeCollectionMap -> {
      Collection<NodeGroup> groups = nodeCollectionMap.get(node);
      if (groups.size() == 0 || groups.size() == 1 && groups.stream().findAny().get().getKey().equals(NamespacedKey.fromString("pathfinder:global"))) {
        return;
      }

      Location location = BukkitVectorUtils.toBukkit(node.getLocation()).add(0, 0.3, 0);
      location.setDirection(player.getLocation().clone().subtract(location).toVector().multiply(new Vector(1, 0, 1)));
      TextDisplay display = ctx.playerSpace.spawn(location, TextDisplay.class);

      Context.NodeContext nodeCtx = new Context.NodeContext(node, display);
      ctx.displayed.put(node.getNodeId(), nodeCtx);

      setText(nodeCtx, groups);
      display.setBillboard(Display.Billboard.VERTICAL);
      ctx.playerSpace.announce();

//    display.setInterpolationDuration(animationTickDuration);
//    display.setInterpolationDelay(-1);
//    display.setTransformation(new Transformation(
//        new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf()
//    ));
    });
  }

  private CompletableFuture<Void> setText(Context.NodeContext context, Collection<NodeGroup> groups) {
//    Component component = Component.join(
//        JoinConfiguration.newlines(),
//        StorageUtil.getGroups(node).stream()
//            .map(NodeGroup::getKey)
//            .map(NamespacedKey::toString)
//            .map(Component::text)
//            .toArray(Component[]::new)
//    );
//    display.setText(serializer.serialize(component));

    String str = groups.stream()
        .map(NodeGroup::getKey).map(NamespacedKey::getKey)
        .filter(s -> !s.equals("global"))
        .collect(Collectors.joining(", "));
    context.display().setText(str);
    return CompletableFuture.completedFuture(null);
  }

  private void hideText(Node node, Player player) {
    Context ctx = context(player);
    Context.NodeContext nodeCtx = ctx.displayed.remove(node.getNodeId());
    if (nodeCtx == null) {
      return;
    }
//    display.setInterpolationDelay(-1);
//    display.setTransformation(new Transformation(
//        new Vector3f(), new Quaternionf(), new Vector3f(0, 0, 0), new Quaternionf()
//    ));
//    Bukkit.getScheduler().runTaskLater(plugin, () -> {
//      if (!displayed.getOrDefault(player.getUniqueId(), Collections.emptyMap()).containsKey(node)) {
//      }
//    }, animationTickDuration);

    nodeCtx.display.remove();
    ctx.playerSpace.announce();
  }

  private boolean holdsGroupTools(Player player) {
    int slot = player.getInventory().getHeldItemSlot();
    return slot == 1 || slot == 2;
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.runAsync(() -> {
      Context ctx = context(player.unwrap());
      ctx.rendered.clear();
      for (Context.NodeContext c : ctx.displayed.values()) {
        hideText(c.node(), player.unwrap());
      }
      ctx.displayed.clear();
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      Context ctx = context(player.unwrap());
      ctx.rendered.addAll(nodes);
      Player p = player.unwrap();
      for (Node node : nodes) {
        evaluate(node, p);
      }
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {

      Context ctx = context(player.unwrap());
      ctx.rendered.removeAll(nodes);
      for (Node node : nodes) {
        hideText(node, player.unwrap());
      }
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }
}
