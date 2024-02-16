package de.cubbossa.pathapi.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.UUID;

public interface PathPlayer<P> {

    UUID getUniqueId();

    Class<P> getPlayerClass();

    String getName();

    Component getDisplayName();

    Location getLocation();

    boolean hasPermission(String permission);

    P unwrap();

    void sendMessage(ComponentLike message);
}
