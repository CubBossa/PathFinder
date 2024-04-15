package de.cubbossa.pathfinder.misc;

import de.cubbossa.disposables.Disposable;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public interface PathPlayer<P> extends Disposable {

    static <P> PathPlayer<? extends P> wrap(P player) {
        return PathPlayerProvider.<P>get().wrap(player);
    }

    static <P> PathPlayer<? extends P> wrap(UUID uuid) {
        return PathPlayerProvider.<P>get().wrap(uuid);
    }

    static <P> PathPlayer<? extends P> consoleSender() {
        return PathPlayerProvider.<P>get().consoleSender();
    }

    UUID getUniqueId();

    Class<P> getPlayerClass();

    String getName();

    Component getDisplayName();

    Location getLocation();

    boolean hasPermission(String permission);

    P unwrap();

    void sendMessage(ComponentLike message);
}
