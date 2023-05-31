package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.splinelib.util.BezierVector;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class NodeUtils {

  /**
   * Lists all waypoints of a certain selection.
   */
  public static void onList(Player player, NodeSelection selection, Pagination pagination) {

    String selector;
    if (selection.getMeta() != null) {
      selector = selection.getMeta().selector();
    } else {
      selector = "@n";
    }

    TagResolver resolver = Placeholder.parsed("selector", selector);

    CommandUtils.printList(
        player, pagination, new ArrayList<>(selection),
        n -> {
          Collection<UUID> neighbours = n.getEdges().stream().map(Edge::getEnd).toList();
          Collection<Node> resolvedNeighbours =
              PathFinderProvider.get().getStorage().loadNodes(neighbours).join();

          TagResolver r = TagResolver.builder()
              .tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
              .resolvers(Messages.formatter().vector("position", n.getLocation()))
              .resolver(Placeholder.unparsed("world", n.getLocation().getWorld().getName()))
              .resolver(Placeholder.component("edges", Messages.formatNodeSelection(player, resolvedNeighbours)))
              .build();
          BukkitUtils.wrap(player).sendMessage(Messages.CMD_N_LIST_ELEMENT.formatted(r));
        },
        Messages.CMD_N_LIST_HEADER.formatted(resolver),
        Messages.CMD_N_LIST_FOOTER.formatted(resolver));
  }

  public static List<BezierVector> toSpline(LinkedHashMap<Node, Double> path, boolean shortenIfOverlapping) {

    if (path.size() < 1) {
      throw new IllegalArgumentException("Path to modify must have at least one point.");
    }
    List<BezierVector> vectors = new ArrayList<>();

    Node first = path.keySet().iterator().next();
    vectors.add(new BezierVector(
        CommonPathFinder.SPLINES.convertToVector(first.getLocation()),
        CommonPathFinder.SPLINES.convertToVector(first.getLocation()),
        CommonPathFinder.SPLINES.convertToVector(first.getLocation())
    ));

    Node prev = null;
    double sPrev = 1;
    Node curr = null;
    double sCurr = 1;
    Vector vNext = null;


    for (Map.Entry<Node, Double> entry : path.entrySet()) {
      Node next = entry.getKey();
      Double sNext = entry.getValue();
      if (prev != null) {
        vectors.add(toBezierVector(prev, curr, next, sPrev, sCurr, sNext, shortenIfOverlapping ?
            new TangentModifier(1, 0) : null));
      }
      prev = curr;
      sPrev = sCurr;
      curr = next;
      sCurr = sNext;
      vNext = next.getLocation();
    }
    vectors.add(new BezierVector(
        CommonPathFinder.SPLINES.convertToVector(vNext),
        CommonPathFinder.SPLINES.convertToVector(vNext),
        CommonPathFinder.SPLINES.convertToVector(vNext)));
    return vectors;
  }

  public static BezierVector toBezierVector(
      Node previous, Node current, Node next,
      double strengthPrevious, double strengthCurrent, double strengthNext,
      @Nullable TangentModifier tangentModifier) {
    Vector vPrevious = previous.getLocation();
    Vector vCurrent = current.getLocation();
    Vector vNext = next.getLocation();

    // make both same distance to vCurrent
    vPrevious = vCurrent.clone().add(vPrevious.clone().subtract(vCurrent).normalize());
    vNext = vCurrent.clone().add(vNext.clone().subtract(vCurrent).normalize());

    // dir is now independent of the distance to neighbouring points
    Vector dir = vNext.clone().subtract(vPrevious).normalize();
    double sCurrentPrev = strengthCurrent;
    double sCurrentNext = strengthCurrent;

    if (tangentModifier != null) {
      double distPrevious = vCurrent.distance(previous.getLocation());
      if (sCurrentPrev + strengthPrevious > distPrevious) {
        sCurrentPrev = distPrevious * sCurrentPrev / (strengthPrevious + sCurrentPrev
            + tangentModifier.staticOffset()) * tangentModifier.relativeOffset();
      }
      double distNext = vCurrent.distance(next.getLocation());
      if (sCurrentNext + strengthNext > distNext) {
        sCurrentNext =
            distNext * sCurrentNext / (strengthNext + sCurrentNext + tangentModifier.staticOffset())
                * tangentModifier.relativeOffset();
      }
    }

    return new BezierVector(
        CommonPathFinder.SPLINES.convertToVector(vCurrent),
        CommonPathFinder.SPLINES.convertToVector(
            vCurrent.clone().add(dir.clone().multiply(-1 * sCurrentPrev))),
        CommonPathFinder.SPLINES.convertToVector(vCurrent.clone().add(dir.multiply(sCurrentNext)))
    );
  }

  public record TangentModifier(double relativeOffset, double staticOffset) {
  }
}
