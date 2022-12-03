package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTermHolder;
import de.cubbossa.pathfinder.module.visualizing.query.SimpleSearchTerm;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

@Getter
@Setter
public class NodeGroup extends HashSet<Groupable> implements Keyed, Named, Discoverable, Navigable, SearchTermHolder {

	private final NamespacedKey key;
	private String nameFormat;
	private Component displayName;
	private @Nullable String permission = null;
	private boolean discoverable = true;
	private boolean navigable = true;
	private float findDistance = 1.5f;
	private Collection<SearchTerm> searchTerms;

	public NodeGroup(NamespacedKey key, String nameFormat) {
		this(key, nameFormat, new HashSet<>());
	}

	public NodeGroup(NamespacedKey key, String nameFormat, Collection<Waypoint> nodes) {
		super(nodes);
		this.key = key;
		this.setNameFormat(nameFormat);
		this.searchTerms = new HashSet<>();
	}

	public void setNameFormat(String nameFormat) {
		this.nameFormat = nameFormat;
		this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
	}

	@Override
	public boolean add(Groupable node) {
		node.addGroup(this);
		return super.add(node);
	}

	@Override
	public boolean addAll(Collection<? extends Groupable> c) {
		for (Groupable g : c) {
			g.addGroup(this);
		}
		return super.addAll(c);
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Groupable groupable) {
			groupable.removeGroup(this);
			return super.remove(o);
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o : c) {
			if (o instanceof Groupable groupable) {
				groupable.removeGroup(this);
				remove(this);
			}
		}
		return true;
	}

	@Override
	public Collection<Node> getGroup() {
		return new HashSet<>(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NodeGroup group)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return key.equals(group.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean fulfillsDiscoveringRequirements(Player player) {
		if (!discoverable) {
			return false;
		}
		if (permission != null && !player.hasPermission(permission)) {
			return false;
		}
		for (Node node : this) {
			if (node == null) {
				PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Node is null");
				continue;
			}
			float dist = DiscoverHandler.getInstance().getDiscoveryDistance(player.getUniqueId(), node);
			if (node.getLocation().getX() - player.getLocation().getX() > dist) {
				continue;
			}
			if (node.getLocation().distance(player.getLocation()) > dist) {
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	public NamespacedKey getKey() {
		return key;
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
		return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(searchTerm.getIdentifier()) && t.matches(attributes));
	}

	@Override
	public boolean matches(String term) {
		return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(term));
	}

	@Override
	public boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
		return searchTerms.stream().anyMatch(t -> t.getIdentifier().equals(term) && t.matches(attributes));
	}
}
