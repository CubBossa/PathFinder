package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeSelection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NodeSelectionImpl implements NodeSelection {

  private final @Nullable String selectionString;
  private final List<Node> domain;

  public NodeSelectionImpl(Collection<Node> nodes, @NotNull String appliedFilter) {
    domain = List.copyOf(nodes);
    this.selectionString = appliedFilter;
  }

  public NodeSelectionImpl(Collection<Node> nodes) {
    domain = new ArrayList<>(nodes);
    this.selectionString = null;
  }

  public NodeSelectionImpl(Node... nodes) {
    domain = new ArrayList<>(List.of(nodes));
    this.selectionString = null;
  }

  @Nullable
  @Override
  public String getSelectionString() {
    return selectionString;
  }

  public Collection<UUID> getIds() {
    return this.stream().map(Node::getNodeId).collect(Collectors.toSet());
  }

  @Override
  public int size() {
    return domain.size();
  }

  @Override
  public boolean isEmpty() {
    return domain.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return domain.contains(o);
  }

  @NotNull
  @Override
  public Iterator<Node> iterator() {
    return domain.iterator();
  }

  @NotNull
  @Override
  public Object[] toArray() {
    return domain.toArray();
  }

  @NotNull
  @Override
  public <T> T[] toArray(@NotNull T[] a) {
    return domain.toArray(a);
  }

  @Override
  public boolean add(Node node) {
    return domain.add(node);
  }

  @Override
  public boolean remove(Object o) {
    return domain.remove(o);
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c) {
    return domain.containsAll(c);
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends Node> c) {
    return domain.addAll(c);
  }

  @Override
  public boolean addAll(int index, @NotNull Collection<? extends Node> c) {
    return domain.addAll(c);
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    return domain.removeAll(c);
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    return domain.retainAll(c);
  }

  @Override
  public void clear() {
    domain.clear();
  }

  @Override
  public Node get(int index) {
    return domain.get(index);
  }

  @Override
  public Node set(int index, Node element) {
    return domain.set(index, element);
  }

  @Override
  public void add(int index, Node element) {
    domain.add(index, element);
  }

  @Override
  public Node remove(int index) {
    return domain.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return domain.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return domain.lastIndexOf(o);
  }

  @NotNull
  @Override
  public ListIterator<Node> listIterator() {
    return domain.listIterator();
  }

  @NotNull
  @Override
  public ListIterator<Node> listIterator(int index) {
    return domain.listIterator(index);
  }

  @NotNull
  @Override
  public List<Node> subList(int fromIndex, int toIndex) {
    return domain.subList(fromIndex, toIndex);
  }
}
