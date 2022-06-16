package de.bossascrew.pathfinder.util;

import co.aikar.commands.ConditionFailedException;
import de.bossascrew.core.graphics.ColorUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.data.visualisation.Visualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

@SuppressWarnings("ALL")
@UtilityClass
public class CommandUtils {

    public interface VisualizerProperty<T> {
        Component accept(T visualizer);
    }

    public static final TextColor NULL_COLOR = TextColor.color(ColorUtils.fromHex("99ff99").getRGB());
    public static final Component NULL_COMPONENT = Component.text("null", NULL_COLOR);

    public @Nullable RoadMap getAnyRoadMap(World world) {
        return RoadMapHandler.getInstance().getRoadMaps(world).stream().findFirst().orElse(null);
    }

    public RoadMap getSelectedRoadMap(CommandSender sender) {
        return getSelectedRoadMap(sender, true);
    }

    public RoadMap getSelectedRoadMap(CommandSender sender, boolean cancelIfUnselected) {
        PathPlayer pplayer = PathPlayerHandler.getInstance().getPlayer(sender);
        if (pplayer.getSelectedRoadMap() == null) {
            if (!cancelIfUnselected) {
                return null;
            }
            throw new ConditionFailedException("Du musst eine Straßenkarte ausgewählt haben. (/roadmap select <Straßenkarte>)");
        }
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMap());
        return roadMap;
    }

    public <T extends Visualizer> Component getParentList(Visualizer<T> visualizer) {
        return getParentList(Component.empty(), visualizer, true);
    }

    public <T extends Visualizer> Component getParentList(Component startComponent, Visualizer<T> visualizer, boolean first) {
        Component separator = Component.text(first ? "" : "«", NamedTextColor.DARK_GRAY);
        if (visualizer.getParent() != null) {
            return startComponent.append(separator).append(Component.text(visualizer.getParent().getName(), PathPlugin.COLOR_LIGHT))
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
        Component part = Component.empty().append(separator).append(propertyComp == null ? NULL_COMPONENT : propertyComp.color(PathPlugin.COLOR_LIGHT));
        if (visualizer.getParent() != null && (propertyComp == null || !cancelAtFirstValid)) {
            return component.append(part).append(getPropertyComponent(component, visualizer.getParent(), property, false, cancelAtFirstValid));
        } else {
            return component.append(part);
        }
    }
}
