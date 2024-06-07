package de.cubbossa.pathfinder.node;

public abstract class NodeSelectionProvider {

  protected static NodeSelectionProvider provider;

  protected abstract NodeSelection of(String selection);

  protected abstract NodeSelection of(String selection, Iterable<Node> scope);

  protected abstract NodeSelection of(Iterable<Node> scope);

  protected abstract NodeSelection ofSender(String selection, Object sender);

  protected abstract NodeSelection ofSender(String selection, Iterable<Node> scope, Object sender);
}
