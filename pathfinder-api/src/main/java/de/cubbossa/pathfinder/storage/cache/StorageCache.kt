package de.cubbossa.pathfinder.storage.cache

import com.google.common.base.Preconditions

interface StorageCache<E> {
    fun write(e: E)

    fun invalidate(e: E)

    fun invalidateAll()

    @JvmRecord
    data class CacheCollection<K, V>(val present: Collection<V>, val absent: Collection<K>) {
        companion object {
            fun <K, V> empty(absent: Collection<K>): CacheCollection<K, V> {
                Preconditions.checkNotNull(absent)
                return CacheCollection(HashSet(), absent)
            }
        }
    }

    @JvmRecord
    data class CacheMap<K, V>(val present: Map<K, V>, val absent: Collection<K>) {

        companion object {
            fun <K, V> empty(absent: Collection<K>): CacheMap<K, V> {
                Preconditions.checkNotNull(absent)
                return CacheMap(HashMap(), absent)
            }
        }
    }
}
