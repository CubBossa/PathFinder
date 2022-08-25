package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.data.DatabaseObject;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

/**
 * An interface that ensures the implementing object to be discoverable.
 * This means, that a player can discover the place by fulfilling the implemented preconditions.
 * In most cases this might be the distance to the position of the object.
 * <p>
 * Discoverable extends the {@link Named} interface, which ensures a display name attribute.
 * The name attribute is used to signal the discovering to the player in a formatted way.
 */
public interface Discoverable extends DatabaseObject<NamespacedKey>, Named {

	/**
	 * @return true, if the player fulfills all requirements to discover this {@link Discoverable} instance.
	 */
	boolean fulfillsDiscoveringRequirements(Player player);

	/**
	 * @return The weight of this discoverable in the calculation of the amount of discovered are.
	 */
	default float getDiscoveringWeight() {
		return 1;
	}
}
