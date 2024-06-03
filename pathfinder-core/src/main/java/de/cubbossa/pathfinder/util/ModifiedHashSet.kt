package de.cubbossa.pathfinder.util

import de.cubbossa.pathfinder.Changes
import java.util.function.Predicate

open class ModifiedHashSet<E>(
    val changes: Changes<E> = Changes()
) : HashSet<E>() {

    constructor(changes: Changes<E>, iterable: Collection<E>) : this(changes) {
        val temp: Collection<E> = HashSet(changes.addList)
        addAll(iterable)
        changes.addList.clear()
        changes.addList.addAll(temp)
    }

    constructor(iterable: Collection<E>) : this(Changes<E>(), iterable)

    override fun add(element: E): Boolean {
        if (super.add(element)) {
            changes.addList.add(element)
            return true
        }
        return false
    }

    final override fun addAll(elements: Collection<E>): Boolean {
        changes.addList.addAll(elements)
        return super.addAll(elements)
    }

    override fun remove(element: E): Boolean {
        if (super.remove(element)) {
            changes.removeList.add(element)
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        changes.removeList.addAll(elements)
        return super.removeAll(elements.toSet())
    }

    override fun removeIf(filter: Predicate<in E>): Boolean {
        return super.removeIf { e: E ->
            if (filter.test(e)) {
                changes.removeList.add(e)
                return@removeIf true
            }
            false
        }
    }
}
