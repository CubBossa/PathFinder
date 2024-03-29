package de.cubbossa.pathapi.misc;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

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
