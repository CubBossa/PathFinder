package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NavigableModifier;
import de.cubbossa.pathapi.visualizer.query.SearchQueryAttribute;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathfinder.navigationquery.SearchTermImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class NavigableModifierImpl implements NavigableModifier {

  private final Collection<SearchTerm> searchTerms;

  public NavigableModifierImpl(String... terms) {
    this(Arrays.stream(terms)
        .map(SearchTermImpl::new)
        .collect(Collectors.toList()));
  }

  public NavigableModifierImpl(SearchTerm... terms) {
    this(Arrays.stream(terms).toList());
  }

  public NavigableModifierImpl(Collection<SearchTerm> terms) {
    this.searchTerms = new ArrayList<>(terms);
  }

  public void addSearchTerms(Collection<SearchTerm> searchTerms) {
    this.searchTerms.addAll(searchTerms);
  }

  public void removeSearchTerms(Collection<SearchTerm> searchTerms) {
    this.searchTerms.removeAll(searchTerms);
  }

  public void clearSearchTerms() {
    this.searchTerms.clear();
  }

  public Collection<SearchTerm> getSearchTerms() {
    return new ArrayList<>(searchTerms);
  }

  @Override
  public Collection<String> getSearchTermStrings() {
    return searchTerms.stream().map(SearchTerm::getIdentifier).toList();
  }

  @Override
  public void removeSearchTermStrings(Collection<String> terms) {
    searchTerms.removeIf(searchTerm -> terms.contains(searchTerm.getIdentifier()));
  }

  @Override
  public void addSearchTermStrings(Collection<String> terms) {
    searchTerms.addAll(terms.stream().map(SearchTermImpl::new).toList());
  }

  @Override
  public void clearSearchTermStrings() {
    searchTerms.clear();
  }

  public boolean matches(SearchTerm searchTerm) {
    return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(searchTerm.getIdentifier()));
  }

  public boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes) {
    return searchTerms.stream().anyMatch(
        t -> t.getIdentifier().equals(searchTerm.getIdentifier()) && t.matches(attributes));
  }

  public boolean matches(String term) {
    return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(term));
  }

  public boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
    return searchTerms.stream()
        .anyMatch(t -> t.getIdentifier().equals(term) && t.matches(attributes));
  }

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
