package de.cubbossa.pathfinder.util;

public record IntPair(int x, int y) {

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		return obj instanceof IntPair pair && pair.x == x && pair.y == y;
	}

	@Override
	public int hashCode() {
		return (x << 24) + y;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
}
