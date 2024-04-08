package de.cubbossa.pathfinder.node;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An edge that connects one node to another.
 * Edges are always directed, to create an undirected edge, create two edges instead.
 */
public interface Edge {

  /**
   * @return The uuid of the start node of this edge
   */
  UUID getStart();

  /**
   * @return The uuid of the end node of this edge
   */
  UUID getEnd();

  /**
   * @return The relative cost of this edge additionally to the actual edge length. The edge length is NOT
   * part of weight.
   */
  float getWeight();

  /**
   * @return The start node of this edge as CompletableFuture.
   */
  CompletableFuture<Node> resolveStart();

  /**
   * @return The end node of this edge as CompletableFuture.
   */
  CompletableFuture<Node> resolveEnd();
}
