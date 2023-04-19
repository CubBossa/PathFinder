package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
@Setter
public abstract class AbstractVisualizer<T extends PathVisualizer<T, D, Player>, D, Player>
		implements PathVisualizer<T, D, Player> {

	private final NamespacedKey key;
	private String nameFormat;
	private Component displayName;

	@Nullable
	private String permission = null;
	private ItemStack displayItem = new ItemStack(Material.REDSTONE);
	private int interval = 1;

	public AbstractVisualizer(NamespacedKey key, String nameFormat) {
		this.key = key;
		setNameFormat(nameFormat);
	}

	public void setNameFormat(String nameFormat) {
		this.nameFormat = nameFormat;
		this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
	}

	public interface Property<Visualizer extends PathVisualizer<?, ?, ?>, Type> {

		String getKey();

		Class<Type> getType();

		void setValue(Visualizer visualizer, Type value);

		Type getValue(Visualizer visualizer);

		boolean isVisible();
	}

	@Getter
	@RequiredArgsConstructor
	public static class SimpleProperty<Value extends PathVisualizer<?, ?, ?>, Type> implements Property<Value, Type> {
		private final String key;
		private final Class<Type> type;
		private final boolean visible;
		private final Function<Value, Type> getter;

		private final BiConsumer<Value, Type> setter;

		@Override
		public void setValue(Value visualizer, Type value) {
			setter.accept(visualizer, value);
		}
		@Override
		public Type getValue(Value visualizer) {
			return getter.apply(visualizer);
		}
	}
}
