package de.cubbossa.pathfinder.navigation.query;

public record SearchQueryAttribute(String identifier, Comparator comparator, Object value)
    implements
    de.cubbossa.pathfinder.visualizer.query.SearchQueryAttribute {

}
