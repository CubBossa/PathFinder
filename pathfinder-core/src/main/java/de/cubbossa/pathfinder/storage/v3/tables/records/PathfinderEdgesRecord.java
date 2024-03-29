/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.storage.v3.tables.records;


import de.cubbossa.pathfinder.storage.v3.tables.PathfinderEdges;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PathfinderEdgesRecord extends UpdatableRecordImpl<PathfinderEdgesRecord> implements Record3<Integer, Integer, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>pathfinder_edges.start_id</code>.
     */
    public void setStartId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>pathfinder_edges.start_id</code>.
     */
    public Integer getStartId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>pathfinder_edges.end_id</code>.
     */
    public void setEndId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>pathfinder_edges.end_id</code>.
     */
    public Integer getEndId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>pathfinder_edges.weight_modifier</code>.
     */
    public void setWeightModifier(Double value) {
        set(2, value);
    }

    /**
     * Getter for <code>pathfinder_edges.weight_modifier</code>.
     */
    public Double getWeightModifier() {
        return (Double) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Integer, Integer> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, Integer, Double> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, Integer, Double> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return PathfinderEdges.PATHFINDER_EDGES.START_ID;
    }

    @Override
    public Field<Integer> field2() {
        return PathfinderEdges.PATHFINDER_EDGES.END_ID;
    }

    @Override
    public Field<Double> field3() {
        return PathfinderEdges.PATHFINDER_EDGES.WEIGHT_MODIFIER;
    }

    @Override
    public Integer component1() {
        return getStartId();
    }

    @Override
    public Integer component2() {
        return getEndId();
    }

    @Override
    public Double component3() {
        return getWeightModifier();
    }

    @Override
    public Integer value1() {
        return getStartId();
    }

    @Override
    public Integer value2() {
        return getEndId();
    }

    @Override
    public Double value3() {
        return getWeightModifier();
    }

    @Override
    public PathfinderEdgesRecord value1(Integer value) {
        setStartId(value);
        return this;
    }

    @Override
    public PathfinderEdgesRecord value2(Integer value) {
        setEndId(value);
        return this;
    }

    @Override
    public PathfinderEdgesRecord value3(Double value) {
        setWeightModifier(value);
        return this;
    }

    @Override
    public PathfinderEdgesRecord values(Integer value1, Integer value2, Double value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PathfinderEdgesRecord
     */
    public PathfinderEdgesRecord() {
        super(PathfinderEdges.PATHFINDER_EDGES);
    }

    /**
     * Create a detached, initialised PathfinderEdgesRecord
     */
    public PathfinderEdgesRecord(Integer startId, Integer endId, Double weightModifier) {
        super(PathfinderEdges.PATHFINDER_EDGES);

        setStartId(startId);
        setEndId(endId);
        setWeightModifier(weightModifier);
    }
}
