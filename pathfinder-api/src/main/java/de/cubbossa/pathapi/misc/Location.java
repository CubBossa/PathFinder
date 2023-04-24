package de.cubbossa.pathapi.misc;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public final class Location extends Vector {

  World world;

  public Location(double x, double y, double z, World world) {
    super(x, y, z);
    this.world = world;
  }

  public Vector asVector() {
    return this;
  }

  @Override
  public Location clone() {
    return new Location(x, y, z, world);
  }

  public Location add(double x, double y, double z) {
    return (Location) super.add(x, y, z);
  }

  public Location add(Vector location) {
    return (Location) super.add(location);
  }

  public Location subtract(Vector location) {
    return (Location) super.subtract(location);
  }

  public Location multiply(Vector location) {
    return (Location) super.multiply(location);
  }

  public Location multiply(double value) {
    return (Location) super.multiply(value);
  }

  public Location divide(Vector location) {
    return (Location) super.divide(location);
  }

  public Location divide(double value) {
    return (Location) super.divide(value);
  }

  public Location normalize() {
    return divide(length());
  }

  @Override
  public String toString() {
    return "Location{" +
        "world=" + world.getName() +
        ", x=" + x +
        ", y=" + y +
        ", z=" + z +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Location l = (Location) o;
    return world.equals(l.world) && l.getX() == x && l.getY() == y && l.getZ() == z;
  }

  @Override
  public int hashCode() {
    return Objects.hash(world, x, y, z);
  }
}
