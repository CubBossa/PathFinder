package de.bossascrew.pathfinder.events;

import de.bossascrew.pathfinder.data.findable.Findable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Date;
import java.util.UUID;

public class NodeGroupFindEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final UUID playerId;
    @Getter
    private final int groupId;
    @Getter
    @Setter
    private Findable node;
    @Getter
    @Setter
    private Date date;

    private boolean cancelled;

    public NodeGroupFindEvent(UUID playerId, Findable node, int groupId, Date date) {
        this.playerId = playerId;
        this.node = node;
        this.date = date;
        this.groupId = groupId;
    }


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
