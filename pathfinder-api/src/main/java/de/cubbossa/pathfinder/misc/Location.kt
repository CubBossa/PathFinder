package de.cubbossa.pathfinder.misc

import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
class Location(
    x: Double,
    y: Double,
    z: Double,
    var world: World
) : Vector(x, y, z) {
    fun asVector(): Vector {
        return this
    }

    fun toBlockCoordinates(): Location {
        return Location(
            x.toInt().toDouble(), y.toInt().toDouble(), z.toInt().toDouble(), world
        )
    }

    override fun clone(): Location {
        return Location(x, y, z, world)
    }

    override fun add(x: Double, y: Double, z: Double): Location {
        return super.add(x, y, z) as Location
    }

    override fun add(location: Vector): Location {
        return super.add(location) as Location
    }

    override fun subtract(location: Vector): Location {
        return super.subtract(location) as Location
    }

    override fun multiply(location: Vector): Location {
        return super.multiply(location) as Location
    }

    override fun multiply(value: Double): Location {
        return super.multiply(value) as Location
    }

    override fun divide(location: Vector): Location {
        return super.divide(location) as Location
    }

    override fun divide(value: Double): Location {
        return super.divide(value) as Location
    }

    override fun normalize(): Location {
        return divide(length())
    }

    override fun toString(): String {
        return "Location{" +
                "world=" + world.name +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val l = o as Location
        return world == l.world && l.x == x && l.y == y && l.z == z
    }

    override fun hashCode(): Int {
        return Objects.hash(world, x, y, z)
    }
}
