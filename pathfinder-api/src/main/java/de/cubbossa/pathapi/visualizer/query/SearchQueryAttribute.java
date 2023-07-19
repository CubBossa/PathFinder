package de.cubbossa.pathapi.visualizer.query;

public interface SearchQueryAttribute {

  String identifier();

  Comparator comparator();

  Object value();

  enum Comparator {
    EQUALS, NOT_EQUALS, GREATER, LESS, GREATER_THAN, LESS_THAN;

    public static Comparator fromString(String value) {
      return switch (value) {
        case "!=" -> NOT_EQUALS;
        case "<" -> LESS;
        case "<=" -> LESS_THAN;
        case ">=" -> GREATER_THAN;
        case ">" -> GREATER;
        default -> EQUALS;
      };
    }
  }
}
