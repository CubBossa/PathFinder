package de.bossascrew.pathfinder.events;

import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.findable.Node;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

public class NodeGroupFindEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final UUID playerId;
    @Getter
    private final Node triggeringFindable;
    @Getter
    @Setter
    private @Nullable FindableGroup group;
    @Getter
    @Setter
    private Date date;
    @Getter
    @Setter
    private boolean cancelled;

    public NodeGroupFindEvent(UUID playerId, FindableGroup group, Node triggeringFindable, Date date) {
        this.playerId = playerId;
        this.group = group;
        this.triggeringFindable = triggeringFindable;
        this.date = date;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
