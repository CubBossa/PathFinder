package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathapi.visualizer.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.navigationquery.SimpleSearchTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class NavigableModifier implements Modifier {

  private final Collection<SearchTerm> searchTerms;

  public NavigableModifier(String... terms) {
    this(Arrays.stream(terms)
        .map(SimpleSearchTerm::new)
        .collect(Collectors.toList()));
  }

  public NavigableModifier(SearchTerm... terms) {
    this(Arrays.stream(terms).toList());
  }

  public NavigableModifier(Collection<SearchTerm> terms) {
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

  public Collection<String> getSearchTermStrings() {
    return searchTerms.stream().map(SearchTerm::getIdentifier).toList();
  }

  public void removeSearchTermStrings(Collection<String> terms) {
    searchTerms.removeIf(searchTerm -> terms.contains(searchTerm.getIdentifier()));
  }

  public void addSearchTermStrings(Collection<String> terms) {
    searchTerms.addAll(terms.stream().map(SimpleSearchTerm::new).toList());
  }

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
}
