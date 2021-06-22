package de.bossascrew.pathfinder.util;

import de.bossascrew.core.graphics.ColorUtils;
import de.bossascrew.pathfinder.data.visualisation.Visualizer;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@SuppressWarnings("ALL")
@UtilityClass
public class VisualizerUtils {

    public interface VisualizerProperty<T> {
        Component accept(T visualizer);
    }

    public static final TextColor NULL_COLOR = TextColor.color(ColorUtils.fromHex("99ff99").getRGB());
    public static final Component NULL_COMPONENT = Component.text("null", NULL_COLOR);

    public <T extends Visualizer> Component getParentList(Visualizer<T> visualizer) {
        return getParentList(Component.empty(), visualizer, true);
    }

    public <T extends Visualizer> Component getParentList(Component startComponent, Visualizer<T> visualizer, boolean first) {
        Component separator = Component.text(first ? "" : "«", NamedTextColor.DARK_GRAY);
        if (visualizer.getParent() != null) {
            return startComponent.append(separator).append(Component.text(visualizer.getParent().getName(), NamedTextColor.GREEN))
                    .append(getParentList(startComponent, visualizer.getParent(), false));
        }
        return startComponent;
    }

    public <T extends Visualizer> Component getPropertyComponent(Visualizer<T> visualizer, VisualizerProperty<T> property) {
        return getPropertyComponent(Component.empty(), visualizer, property, true, true);
    }

    public <T extends Visualizer> Component getPropertyComponent(Component component, Visualizer<T> visualizer, VisualizerProperty<T> property, boolean first, boolean cancelAtFirstValid) {
        Component separator = Component.text(first ? "" : "«", NamedTextColor.DARK_GRAY);
        if (visualizer == null) {
            return component;
        }
        Component propertyComp = property.accept((T) visualizer);
        Component part = Component.empty().append(separator).append(propertyComp == null ? NULL_COMPONENT : propertyComp.color(NULL_COLOR));
        if (visualizer.getParent() != null && (propertyComp == null || !cancelAtFirstValid)) {
            return component.append(part).append(getPropertyComponent(component, visualizer.getParent(), property, false, cancelAtFirstValid));
        } else {
            return component.append(part);
        }
    }


}
