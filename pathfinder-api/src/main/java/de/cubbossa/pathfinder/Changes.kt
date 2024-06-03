package de.cubbossa.pathfinder

import java.util.concurrent.ConcurrentHashMap

class Changes<E> {
    val addList: MutableCollection<E> = ConcurrentHashMap.newKeySet(16)
    val removeList: MutableCollection<E> = ConcurrentHashMap.newKeySet(16)

    fun flush() {
        addList.clear()
        removeList.clear()
    }
}
