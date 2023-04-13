package de.cubbossa.pathfinder.module.visualizing.query;

public record SearchQueryAttribute(String identifier, Comparator comparator, Object value)
    implements
    de.cubbossa.pathfinder.api.visualizer.query.SearchQueryAttribute {

}
