package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.misc.World
import java.util.*

fun interface WorldLoader {
    fun loadWorld(uuid: UUID): World?
}
