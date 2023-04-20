package de.cubbossa.pathfinder.nodeselection;

import java.text.ParseException;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * An interval that is defined by a min and a max value.
 * The max value must at least be as high as the min value, if not higher.
 */
public class NumberRange {

  private static final String DOUBLE_REGEX = "[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?";
  public static final Pattern STRING_FORMAT =
      Pattern.compile("(" + DOUBLE_REGEX + ")?(\\.\\.)?(" + DOUBLE_REGEX + ")?");

  @Getter
  private final Number start;
  @Getter
  private final Number end;

  /**
   * A non-restricting interval from -MAX_DOUBLE to MAX_DOUBLE
   */
  public NumberRange() {
    this(-Double.MAX_VALUE, Double.MAX_VALUE);
  }

  /**
   * A restricting interval with defined start and inclusive end value.
   *
   * @param start The first contained value of the interval
   * @param end   The last contained value of the interval. Must at least be as high as start.
   * @throws IllegalArgumentException if end is smaller than start.
   */
  public NumberRange(Number start, Number end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Creates an interval from a given start value to MAX_DOUBLE.
   *
   * @param start The start value of the interval.
   * @return The NumberRange instance.
   * @throws IllegalArgumentException if end is smaller than start.
   */
  public static NumberRange from(Number start) {
    return new NumberRange(start, Double.MAX_VALUE);
  }

  /**
   * Creates an interval from -MAX_DOUBLE to a given end value.
   *
   * @param end The included end value of the interval.
   * @return The NumberRange instance.
   * @throws IllegalArgumentException if end is smaller than start.
   */
  public static NumberRange to(Number end) {
    return new NumberRange(-Double.MAX_VALUE, end);
  }

  /**
   * A restricting interval with defined start and inclusive end value.
   *
   * @param start The first contained value of the interval
   * @param end   The last contained value of the interval. Must at least be as high as start.
   * @throws IllegalArgumentException if end is smaller than start.
   */
  public static NumberRange range(Number start, Number end) {
    if (start.doubleValue() > end.doubleValue()) {
      throw new IllegalArgumentException("The end attribute of a number range must be at least"
          + "equals the size of the start attribute.");
    }
    return new NumberRange(start, end);
  }

  /**
   * Parses an interval.
   * Ranges can be
   * <ul>
   * <li>single values: "5".</li>
   * <li>only restricted to the left: "5..", meaning 5 or higher.</li>
   * <li>only restricted to the right: "..5", meaning 5 or less.</li>
   * <li>restricted: "1..5", meaning between 1 and 5.</li>
   * </ul>
   *
   * <p>Numbers can be represented as
   * <ul>
   * <li>ints (1, 2, 3)</li>
   * <li>doubles (1.2, 3.14)</li>
   * <li>powers (1.23e12)</li>
   * </ul>
   *
   * @param value The string value to parse.
   * @return A NumberRange instance.
   */
  public static NumberRange fromString(String value) {
    try {
      return parse(value);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses an interval.
   * Ranges can be
   * <ul>
   * <li>single values: "5".</li>
   * <li>only restricted to the left: "5..", meaning 5 or higher.</li>
   * <li>only restricted to the right: "..5", meaning 5 or less.</li>
   * <li>restricted: "1..5", meaning between 1 and 5.</li>
   * </ul>
   *
   * <p>Numbers can be represented as
   * <ul>
   * <li>ints (1, 2, 3)</li>
   * <li>doubles (1.2, 3.14)</li>
   * <li>powers (1.23e12)</li>
   * </ul>
   *
   * @param value The string value to parse.
   * @return A NumberRange instance.
   * @throws ParseException If the NumberRange is not of the given format.
   */
  public static NumberRange parse(String value) throws ParseException {
    Matcher matcher = STRING_FORMAT.matcher(value);
    if (!matcher.matches()) {
      throw new ParseException(value, 0);
    }
    MatchResult result = matcher.toMatchResult();

    boolean ranged = matcher.group(4) != null;
    double start = -Double.MAX_VALUE;
    double end = Double.MAX_VALUE;
    String startString = result.group(1);
    if (startString != null) {
      start = Double.parseDouble(startString);
      if (!ranged) {
        end = start;
      }
    }
    String endString = result.group(5);
    if (endString != null) {
      end = Double.parseDouble(endString);
      if (!ranged) {
        start = end;
      }
    }
    return new NumberRange(start, end);
  }

  /**
   * Checks, if a value is inside bounds of this interval.
   *
   * @param value The value to check.
   * @return true if is contained in interval.
   */
  public boolean contains(Number value) {
    return value.doubleValue() >= start.doubleValue()
        && value.doubleValue() <= end.doubleValue();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NumberRange range
        && range.start.doubleValue() == start.doubleValue()
        && range.end.doubleValue() == end.doubleValue();
  }

  @Override
  public String toString() {
    boolean ranged = !Objects.equals(start, end);
    String prettyStart = start.doubleValue() == -Double.MAX_VALUE ? "" : start.toString();
    String prettyEnd = end.doubleValue() == Double.MAX_VALUE ? "" : end.toString();
    return ranged
        ? prettyStart + ".." + prettyEnd
        : prettyStart;
  }
}
