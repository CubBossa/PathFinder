package de.cubbossa.pathfinder.core.nodegroup.modifier;

import de.cubbossa.pathfinder.module.visualizing.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import de.cubbossa.pathfinder.module.visualizing.query.SimpleSearchTerm;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record NavigableModifier(String... searchTerms) implements GroupModifier {
	private static final NamespacedKey KEY = NamespacedKey.fromString("pathfinder:navigable");
	@Override
	@NotNull
	public NamespacedKey getKey() {
		return KEY;
	}

	@Override
	public void addSearchTerms(Collection<SearchTerm> searchTerms) {

	}

	@Override
	public void removeSearchTerms(Collection<SearchTerm> searchTerms) {

	}

	@Override
	public void clearSearchTerms() {

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

	@Override
	public boolean matches(SearchTerm searchTerm) {
		return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(searchTerm.getIdentifier()));
	}

	@Override
	public boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes) {
		return searchTerms.stream().anyMatch(
						t -> t.getIdentifier().equals(searchTerm.getIdentifier()) && t.matches(attributes));
	}

	@Override
	public boolean matches(String term) {
		return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(term));
	}

	@Override
	public boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
		return searchTerms.stream()
						.anyMatch(t -> t.getIdentifier().equals(term) && t.matches(attributes));
	}
}
