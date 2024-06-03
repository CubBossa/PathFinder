package de.cubbossa.pathfinder.misc

import lombok.Getter
import java.util.function.Predicate
import java.util.regex.Pattern

@Getter
class NamespacedKey(
    private val namespace: String,
    private val key: String
) {

    override fun toString(): String {
        return namespace + SEPARATOR + key
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as NamespacedKey
        return namespace == that.namespace && key == that.key
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    companion object {
        private val PATTERN: Pattern = Pattern.compile("[a-z0-9_-]+:[a-z0-9_-]+")
        private val PATTERN_TEST: Predicate<String> = PATTERN.asMatchPredicate()
        private const val SEPARATOR = ':'

        @JvmStatic
        fun fromString(value: String): NamespacedKey {
            require(PATTERN_TEST.test(value)) { "NamespacedKey must match pattern '" + PATTERN.pattern() + "'. Input: '" + value + "'." }
            val splits = value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return NamespacedKey(splits[0], splits[1])
        }
    }
}
