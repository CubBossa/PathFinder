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
public abstract class AbstractVisualizer<T extends PathVisualizer<T, D>, D> implements PathVisualizer<T, D> {

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

	public interface Property<V extends PathVisualizer<?, ?>, T> {

		String getKey();

		Class<T> getType();

		void setValue(V visualizer, T value);

		T getValue(V visualizer);

		boolean isVisible();
	}

	@Getter
	@RequiredArgsConstructor
	public static class SimpleProperty<V extends PathVisualizer<?, ?>, T> implements Property<V, T> {
		private final String key;
		private final Class<T> type;
		private final boolean visible;
		private final Function<V, T> getter;

		private final BiConsumer<V, T> setter;

		@Override
		public void setValue(V visualizer, T value) {
			setter.accept(visualizer, value);
		}
		@Override
		public T getValue(V visualizer) {
			return getter.apply(visualizer);
		}
	}
}
