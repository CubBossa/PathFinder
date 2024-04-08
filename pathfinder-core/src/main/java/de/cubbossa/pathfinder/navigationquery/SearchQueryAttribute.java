package de.cubbossa.pathfinder.navigationquery;

public record SearchQueryAttribute(String identifier, Comparator comparator, Object value)
    implements
    de.cubbossa.pathfinder.visualizer.query.SearchQueryAttribute {

}
