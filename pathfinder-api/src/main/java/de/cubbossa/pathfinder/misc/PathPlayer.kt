package de.cubbossa.pathfinder.misc;

import de.cubbossa.disposables.Disposable;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public interface PathPlayer<P> extends Disposable {

    static <P> PathPlayer<P> wrap(P player) {
        return (PathPlayer<P>) PathPlayerProvider.<P>get().wrap(player);
    }

    static <P> PathPlayer<P> wrap(UUID uuid) {
        return (PathPlayer<P>) PathPlayerProvider.<P>get().wrap(uuid);
    }

    static <P> PathPlayer<P> consoleSender() {
        return (PathPlayer<P>) PathPlayerProvider.<P>get().consoleSender();
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
