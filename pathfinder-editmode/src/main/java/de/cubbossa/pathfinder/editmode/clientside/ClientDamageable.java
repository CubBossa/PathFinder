package de.cubbossa.pathfinder.editmode.clientside;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class ClientDamageable extends ClientEntity implements Damageable {

  private double maxHealth;
  private double health;
  private double absorptionAmount;

  public ClientDamageable(PlayerSpace playerSpace, int entityId, EntityType entityType) {
    super(playerSpace, entityId, entityType);
  }

  @Override
  public void damage(double amount) {
    health -= amount;
    if (health <= 0) {
      remove();
    }
  }

  @Override
  public void damage(double amount, @Nullable Entity source) {
    health -= amount;
    if (health <= 0) {
      remove();
    }
  }

  @Override
  public void resetMaxHealth() {
    throw new ClientEntityMethodNotSupportedException();
  }
}
