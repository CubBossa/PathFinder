package de.cubbossa.pathfinder.misc

open class Range(
    val start: Int = 0,
    val limit: Int = 1
) {
    val endExclusive: Int
        get() = start + limit

    override fun toString(): String {
        return "Pagination{" +
                "offset=" + start +
                ", limit=" + limit +
                '}'
    }
}
