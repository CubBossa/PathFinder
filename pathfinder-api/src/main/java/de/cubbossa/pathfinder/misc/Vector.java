package de.cubbossa.pathfinder.misc;

import java.util.Objects;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class Vector implements Cloneable {

  double x;
  double y;
  double z;

  public Vector add(double x, double y, double z) {
    this.x += x;
    this.y += y;
    this.z += z;
    return this;
  }

  public Vector add(Vector location) {
    this.x += location.x;
    this.y += location.y;
    this.z += location.z;
    return this;
  }

  public Vector subtract(Vector location) {
    this.x -= location.x;
    this.y -= location.y;
    this.z -= location.z;
    return this;
  }

  public Vector multiply(Vector location) {
    this.x *= location.x;
    this.y *= location.y;
    this.z *= location.z;
    return this;
  }

  public Vector multiply(double value) {
    this.x *= value;
    this.y *= value;
    this.z *= value;
    return this;
  }

  public Vector divide(Vector location) {
    this.x /= location.x;
    this.y /= location.y;
    this.z /= location.z;
    return this;
  }

  public Vector divide(double value) {
    this.x /= value;
    this.y /= value;
    this.z /= value;
    return this;
  }

  public Vector normalize() {
    return divide(length());
  }

  public double dot(@NotNull Vector other) {
    return this.x * other.x + this.y * other.y + this.z * other.z;
  }

  public Vector crossProduct(@NotNull Vector o) {
    double newX = this.y * o.z - o.y * this.z;
    double newY = this.z * o.x - o.z * this.x;
    double newZ = this.x * o.y - o.x * this.y;
    this.x = newX;
    this.y = newY;
    this.z = newZ;
    return this;
  }

  public double distanceSquared(Vector location) {
    return clone().subtract(location).lengthSquared();
  }

  public double distance(Vector location) {
    return clone().subtract(location).length();
  }

  public double length() {
    return Math.sqrt(lengthSquared());
  }

  public double lengthSquared() {
    return Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
  }

  public Vector clone() {
    return new Vector(x, y, z);
  }

  public double getX() {
    return this.x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return this.y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getZ() {
    return this.z;
  }

  public void setZ(double z) {
    this.z = z;
  }

  public Location toLocation(World world) {
    return new Location(x, y, z, world);
  }

  @Override
  public String toString() {
    return "Vector{" +
        "x=" + x +
        ", y=" + y +
        ", z=" + z +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vector vector = (Vector) o;
    return Double.compare(vector.x, x) == 0 && Double.compare(vector.y, y) == 0 && Double.compare(vector.z, z) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }
}
