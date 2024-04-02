package de.cubbossa.pathapi.misc;

import de.cubbossa.disposables.Disposable;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.UUID;

public interface PathPlayer<P> extends Disposable {

    UUID getUniqueId();

    Class<P> getPlayerClass();

    String getName();

    Component getDisplayName();

    Location getLocation();

    boolean hasPermission(String permission);

    P unwrap();

    void sendMessage(ComponentLike message);
}
