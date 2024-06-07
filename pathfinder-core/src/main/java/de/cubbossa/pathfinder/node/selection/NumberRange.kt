package de.cubbossa.pathfinder.node.selection

import lombok.Getter
import java.text.ParseException
import java.util.regex.Pattern

/**
 * An interval that is defined by a min and a max value.
 * The max value must at least be as high as the min value, if not higher.
 */
class NumberRange
/**
 * A non-restricting interval from -MAX_DOUBLE to MAX_DOUBLE
 */ @JvmOverloads constructor(
    @field:Getter private val start: Number = -Double.MAX_VALUE,
    @field:Getter private val end: Number = Double.MAX_VALUE
) {
    /**
     * A restricting interval with defined start and inclusive end value.
     *
     * @param start The first contained value of the interval
     * @param end   The last contained value of the interval. Must at least be as high as start.
     * @throws IllegalArgumentException if end is smaller than start.
     */

    /**
     * Checks, if a value is inside bounds of this interval.
     *
     * @param value The value to check.
     * @return true if is contained in interval.
     */
    fun contains(value: Number): Boolean {
        return (value.toDouble() >= start.toDouble()
                && value.toDouble() <= end.toDouble())
    }

    override fun equals(obj: Any?): Boolean {
        return obj is NumberRange && obj.start.toDouble() == start.toDouble() && obj.end.toDouble() == end.toDouble()
    }

    override fun toString(): String {
        val ranged = start != end
        val prettyStart = if (start.toDouble() == -Double.MAX_VALUE) "" else start.toString()
        val prettyEnd = if (end.toDouble() == Double.MAX_VALUE) "" else end.toString()
        return if (ranged
        ) "$prettyStart..$prettyEnd"
        else prettyStart
    }

    companion object {
        private const val DOUBLE_REGEX = "[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?"
        val STRING_FORMAT: Pattern =
            Pattern.compile("(" + DOUBLE_REGEX + ")?(\\.\\.)?(" + DOUBLE_REGEX + ")?")

        /**
         * Creates an interval from a given start value to MAX_DOUBLE.
         *
         * @param start The start value of the interval.
         * @return The NumberRange instance.
         * @throws IllegalArgumentException if end is smaller than start.
         */
        @JvmStatic
        fun from(start: Number): NumberRange {
            return NumberRange(start, Double.MAX_VALUE)
        }

        /**
         * Creates an interval from -MAX_DOUBLE to a given end value.
         *
         * @param end The included end value of the interval.
         * @return The NumberRange instance.
         * @throws IllegalArgumentException if end is smaller than start.
         */
        @JvmStatic
        fun to(end: Number): NumberRange {
            return NumberRange(-Double.MAX_VALUE, end)
        }

        /**
         * A restricting interval with defined start and inclusive end value.
         *
         * @param start The first contained value of the interval
         * @param end   The last contained value of the interval. Must at least be as high as start.
         * @throws IllegalArgumentException if end is smaller than start.
         */
        @JvmStatic
        fun range(start: Number, end: Number): NumberRange {
            require(!(start.toDouble() > end.toDouble())) {
                ("The end attribute of a number range must be at least"
                        + "equals the size of the start attribute.")
            }
            return NumberRange(start, end)
        }

        /**
         * Parses an interval.
         * Ranges can be
         *
         *  * single values: "5".
         *  * only restricted to the left: "5..", meaning 5 or higher.
         *  * only restricted to the right: "..5", meaning 5 or less.
         *  * restricted: "1..5", meaning between 1 and 5.
         *
         *
         *
         * Numbers can be represented as
         *
         *  * ints (1, 2, 3)
         *  * doubles (1.2, 3.14)
         *  * powers (1.23e12)
         *
         *
         * @param value The string value to parse.
         * @return A NumberRange instance.
         */
        @JvmStatic
        fun fromString(value: String?): NumberRange {
            try {
                return parse(value)
            } catch (e: ParseException) {
                throw RuntimeException(e)
            }
        }

        /**
         * Parses an interval.
         * Ranges can be
         *
         *  * single values: "5".
         *  * only restricted to the left: "5..", meaning 5 or higher.
         *  * only restricted to the right: "..5", meaning 5 or less.
         *  * restricted: "1..5", meaning between 1 and 5.
         *
         *
         *
         * Numbers can be represented as
         *
         *  * ints (1, 2, 3)
         *  * doubles (1.2, 3.14)
         *  * powers (1.23e12)
         *
         *
         * @param value The string value to parse.
         * @return A NumberRange instance.
         * @throws ParseException If the NumberRange is not of the given format.
         */
        @JvmStatic
        @Throws(ParseException::class)
        fun parse(value: String?): NumberRange {
            val matcher = STRING_FORMAT.matcher(value)
            if (!matcher.matches()) {
                throw ParseException(value, 0)
            }
            val result = matcher.toMatchResult()

            val ranged = matcher.group(4) != null
            var start = -Double.MAX_VALUE
            var end = Double.MAX_VALUE
            val startString = result.group(1)
            if (startString != null) {
                start = startString.toDouble()
                if (!ranged) {
                    end = start
                }
            }
            val endString = result.group(5)
            if (endString != null) {
                end = endString.toDouble()
                if (!ranged) {
                    start = end
                }
            }
            return NumberRange(start, end)
        }
    }
}
