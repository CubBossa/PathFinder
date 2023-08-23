package de.cubbossa.pathfinder.editmode.clientside;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter

public class ClientEntity implements Entity, UntickedEntity {

  static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

  final int entityId;
  final UUID uniqueId;
  final EntityType type;
  final PlayerSpace playerSpace;

  boolean alive = true;
  Location location = new Location(null, 0, 0, 0);
  Vector velocity = new Vector(0, 0, 0);
  double height = 0;
  double width = 0;
  boolean visible = true;
  boolean invulnerable = true;
  boolean glowing = false;
  boolean gravity = true;
  boolean silent = true;

  boolean customNameVisible = false;
  Component customName;

  // changed properties
  boolean aliveChanged = false;
  boolean locationChanged = false;
  boolean velocityChanged = false;
  boolean metaChanged = false;

  public ClientEntity(PlayerSpace playerSpace, int entityId, EntityType entityType) {
    this.playerSpace = playerSpace;
    this.entityId = entityId;
    this.uniqueId = UUID.randomUUID();
    this.type = entityType;

    // entity not yet spawned
    this.aliveChanged = true;
  }

  public void setCustomName(Component customName) {
    this.customName = customName;
  }

  public void setCustomName(String customName) {
    this.customName = Component.text(customName);
  }

  public String getCustomName() {
    return GSON.serialize(customName);
  }

  @NotNull
  @Override
  public Location getLocation() {
    return location;
  }

  @Nullable
  @Override
  public Location getLocation(@Nullable Location loc) {
    if (loc == null) {
      return null;
    }
    loc.setX(location.getX());
    loc.setY(location.getY());
    loc.setZ(location.getZ());
    return loc;
  }

  @NotNull
  @Override
  public BoundingBox getBoundingBox() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean isOnGround() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean isInWater() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public World getWorld() {
    return getLocation().getWorld();
  }

  @Override
  public void setRotation(float yaw, float pitch) {
    this.location.setYaw(yaw);
    this.location.setPitch(pitch);
    this.locationChanged = true;
  }

  @Override
  public boolean teleport(@NotNull Location location) {
    this.location = location.clone();
    this.locationChanged = true;
    return true;
  }

  @Override
  public boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause) {
    this.location = location.clone();
    this.locationChanged = true;
    return true;
  }

  @Override
  public boolean teleport(@NotNull Entity destination) {
    this.location = destination.getLocation().clone();
    this.locationChanged = true;
    return true;
  }

  @Override
  public boolean teleport(@NotNull Entity destination, @NotNull PlayerTeleportEvent.TeleportCause cause) {
    this.location = destination.getLocation().clone();
    this.locationChanged = true;
    return true;
  }

  @NotNull
  @Override
  public List<Entity> getNearbyEntities(double x, double y, double z) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public int getFireTicks() {
    return 0;
  }

  @Override
  public int getMaxFireTicks() {
    return 0;
  }

  @Override
  public void setFireTicks(int ticks) {

  }

  @Override
  public void setVisualFire(boolean fire) {

  }

  @Override
  public boolean isVisualFire() {
    return false;
  }

  @Override
  public int getFreezeTicks() {
    return 0;
  }

  @Override
  public int getMaxFreezeTicks() {
    return 0;
  }

  @Override
  public void setFreezeTicks(int ticks) {

  }

  @Override
  public boolean isFrozen() {
    return false;
  }

  @Override
  public void remove() {
    alive = false;
    aliveChanged = true;
  }

  @Override
  public boolean isDead() {
    return !alive;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void sendMessage(@NotNull String message) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void sendMessage(@NotNull String... messages) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void sendMessage(@Nullable UUID sender, @NotNull String message) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public Server getServer() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public String getName() {
    return null;
  }

  @Override
  public boolean isPersistent() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setPersistent(boolean persistent) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Nullable
  @Override
  public Entity getPassenger() {
    return null;
  }

  @Override
  public boolean setPassenger(@NotNull Entity passenger) {
    return false;
  }

  @NotNull
  @Override
  public List<Entity> getPassengers() {
    return null;
  }

  @Override
  public boolean addPassenger(@NotNull Entity passenger) {
    return false;
  }

  @Override
  public boolean removePassenger(@NotNull Entity passenger) {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean eject() {
    return false;
  }

  @Override
  public float getFallDistance() {
    return 0;
  }

  @Override
  public void setFallDistance(float distance) {

  }

  @Override
  public void setLastDamageCause(@Nullable EntityDamageEvent event) {

  }

  @Nullable
  @Override
  public EntityDamageEvent getLastDamageCause() {
    return null;
  }

  @Override
  public int getTicksLived() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setTicksLived(int value) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void playEffect(@NotNull EntityEffect type) {

  }

  @NotNull
  @Override
  public Sound getSwimSound() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public Sound getSwimSplashSound() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public Sound getSwimHighSpeedSplashSound() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean isInsideVehicle() {
    return false;
  }

  @Override
  public boolean leaveVehicle() {
    return false;
  }

  @Nullable
  @Override
  public Entity getVehicle() {
    return null;
  }

  @Override
  public void setCustomNameVisible(boolean flag) {

  }

  @Override
  public boolean isCustomNameVisible() {
    return false;
  }

  @Override
  public void setVisibleByDefault(boolean visible) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean isVisibleByDefault() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public int getPortalCooldown() {
    return 0;
  }

  @Override
  public void setPortalCooldown(int cooldown) {

  }

  @NotNull
  @Override
  public Set<String> getScoreboardTags() {
    return null;
  }

  @Override
  public boolean addScoreboardTag(@NotNull String tag) {
    return false;
  }

  @Override
  public boolean removeScoreboardTag(@NotNull String tag) {
    return false;
  }

  @NotNull
  @Override
  public PistonMoveReaction getPistonMoveReaction() {
    return null;
  }

  @NotNull
  @Override
  public BlockFace getFacing() {
    return null;
  }

  @NotNull
  @Override
  public Pose getPose() {
    return null;
  }

  @NotNull
  @Override
  public SpawnCategory getSpawnCategory() {
    return SpawnCategory.MISC;
  }

  @NotNull
  @Override
  public Spigot spigot() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {

  }

  @NotNull
  @Override
  public List<MetadataValue> getMetadata(@NotNull String metadataKey) {
    return null;
  }

  @Override
  public boolean hasMetadata(@NotNull String metadataKey) {
    return false;
  }

  @Override
  public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {

  }

  @Override
  public boolean isPermissionSet(@NotNull String name) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean isPermissionSet(@NotNull Permission perm) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean hasPermission(@NotNull String name) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean hasPermission(@NotNull Permission perm) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Nullable
  @Override
  public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Nullable
  @Override
  public PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void removeAttachment(@NotNull PermissionAttachment attachment) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void recalculatePermissions() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public boolean hasGravity() {
    return gravity;
  }

  @Override
  public boolean isOp() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void setOp(boolean value) {
    throw new ClientEntityMethodNotSupportedException();
  }

  @NotNull
  @Override
  public PersistentDataContainer getPersistentDataContainer() {
    throw new ClientEntityMethodNotSupportedException();
  }

  @Override
  public void update(Collection<Player> viewers) {
    for (Player player : viewers) {
      PacketEventsAPI<?> api = PacketEvents.getAPI();
      if (aliveChanged) {
        if (!alive) {
          api.getPlayerManager().sendPacket(player,
              new WrapperPlayServerDestroyEntities(entityId)
          );
          playerSpace.releaseEntity(this);
        } else {
          api.getPlayerManager().sendPacket(player,
              new WrapperPlayServerSpawnEntity(entityId, Optional.ofNullable(uniqueId), SpigotConversionUtil.fromBukkitEntityType(type),
                  new Vector3d(location.getX(), location.getY(), location.getZ()), 0, 0, 0, 0,
                  Optional.ofNullable(velocity == null ? null : new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ()))
              )
          );
        }
        aliveChanged = false;
        locationChanged = false;
      }
      if (locationChanged) {
        api.getPlayerManager().sendPacket(player,
            new WrapperPlayServerEntityTeleport(entityId, SpigotConversionUtil.fromBukkitLocation(location), true)
        );
        locationChanged = false;
      }
      if (metaChanged) {
        try {
          List<EntityData> data = metaData();
          api.getPlayerManager().sendPacket(player, new WrapperPlayServerEntityMetadata(entityId, data));
        } catch (Throwable t) {
          t.printStackTrace();
        }
        metaChanged = false;
      }
    }
  }

  List<EntityData> metaData() {
    List<EntityData> data = new ArrayList<>();
    // flags
    byte mask = (byte) ((!visible ? 0x20 : 0) | (isGlowing() ? 0x40 : 0));
    if (mask != 0) {
      data.add(new EntityData(0, EntityDataTypes.BYTE, mask));
    }
    // data.add(new EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.ofNullable(getCustomName())));
    if (customNameVisible) {
      data.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
    }
    if (silent) {
      data.add(new EntityData(4, EntityDataTypes.BOOLEAN, true));
    }
    if (!gravity) {
      data.add(new EntityData(5, EntityDataTypes.BOOLEAN, false));
    }
    return data;
  }

}
