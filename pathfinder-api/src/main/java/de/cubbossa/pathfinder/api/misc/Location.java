package de.cubbossa.pathfinder.api.misc;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public final class Location implements Cloneable {

	private double x;
	private double y;
	private double z;
	private UUID world;

	public Location add(Location location) {
		this.x += location.x;
		this.y += location.y;
		this.z += location.z;
		return this;
	}

	public Location subtract(Location location) {
		this.x -= location.x;
		this.y -= location.y;
		this.z -= location.z;
		return this;
	}

	public Location multiply(Location location) {
		this.x *= location.x;
		this.y *= location.y;
		this.z *= location.z;
		return this;
	}

	public Location multiply(double value) {
		this.x *= value;
		this.y *= value;
		this.z *= value;
		return this;
	}

	public Location divide(Location location) {
		this.x /= location.x;
		this.y /= location.y;
		this.z /= location.z;
		return this;
	}

	public Location divide(double value) {
		this.x /= value;
		this.y /= value;
		this.z /= value;
		return this;
	}


	public Location normalize() {
		return divide(length());
	}

	public double distanceSquared(Location location) {
		return clone().subtract(location).lengthSquared();
	}

	public double distance(Location location) {
		return clone().subtract(location).length();
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}

	public double lengthSquared() {
		return Math.pow(x, 2) + Math.pow(y, 2)+ Math.pow(z, 2);
	}

	public Location clone() {
		return new Location(x, y, z, world);
	}
}
