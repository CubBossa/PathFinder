package de.cubbossa.pathfinder.misc

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


open class Vector(
    var x: Double,
    var y: Double,
    var z: Double,
) : Cloneable {

    open fun add(x: Double, y: Double, z: Double): Vector? {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    open fun add(location: Vector): Vector {
        this.x += location.x
        this.y += location.y
        this.z += location.z
        return this
    }

    open fun subtract(location: Vector): Vector {
        this.x -= location.x
        this.y -= location.y
        this.z -= location.z
        return this
    }

    open fun multiply(location: Vector): Vector {
        this.x *= location.x
        this.y *= location.y
        this.z *= location.z
        return this
    }

    open fun multiply(value: Double): Vector {
        this.x *= value
        this.y *= value
        this.z *= value
        return this
    }

    open fun divide(location: Vector): Vector {
        this.x /= location.x
        this.y /= location.y
        this.z /= location.z
        return this
    }

    open fun divide(value: Double): Vector {
        this.x /= value
        this.y /= value
        this.z /= value
        return this
    }

    open fun normalize(): Vector {
        return divide(length())
    }

    fun dot(other: Vector): Double {
        return this.x * other.x + (this.y * other.y) + (this.z * other.z)
    }

    fun crossProduct(o: Vector): Vector {
        val newX = this.y * o.z - o.y * this.z
        val newY = this.z * o.x - o.z * this.x
        val newZ = this.x * o.y - o.x * this.y
        this.x = newX
        this.y = newY
        this.z = newZ
        return this
    }

    fun distanceSquared(location: Vector): Double {
        return clone().subtract(location).lengthSquared()
    }

    fun distance(location: Vector): Double {
        return clone().subtract(location).length()
    }

    fun length(): Double {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Double {
        return x.pow(2.0) + y.pow(2.0) + z.pow(2.0)
    }

    public override fun clone(): Vector {
        return Vector(x, y, z)
    }

    fun toLocation(world: World?): Location {
        return Location(x, y, z, world!!)
    }

    override fun toString(): String {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val vector = other as Vector
        return vector.x.compareTo(x) == 0 && vector.y.compareTo(y) == 0 && vector.z.compareTo(z) == 0
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y, z)
    }
}
