/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq.tables.records;


import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PathfinderNodegroupsRecord extends UpdatableRecordImpl<PathfinderNodegroupsRecord> implements Record2<NamespacedKey, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>pathfinder_nodegroups.key</code>.
     */
    public void setKey(NamespacedKey value) {
        set(0, value);
    }

    /**
     * Getter for <code>pathfinder_nodegroups.key</code>.
     */
    public NamespacedKey getKey() {
        return (NamespacedKey) get(0);
    }

    /**
     * Setter for <code>pathfinder_nodegroups.weight</code>.
     */
    public void setWeight(Double value) {
        set(1, value);
    }

    /**
     * Getter for <code>pathfinder_nodegroups.weight</code>.
     */
    public Double getWeight() {
        return (Double) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<NamespacedKey> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<NamespacedKey, Double> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<NamespacedKey, Double> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<NamespacedKey> field1() {
        return PathfinderNodegroups.PATHFINDER_NODEGROUPS.KEY;
    }

    @Override
    public Field<Double> field2() {
        return PathfinderNodegroups.PATHFINDER_NODEGROUPS.WEIGHT;
    }

    @Override
    public NamespacedKey component1() {
        return getKey();
    }

    @Override
    public Double component2() {
        return getWeight();
    }

    @Override
    public NamespacedKey value1() {
        return getKey();
    }

    @Override
    public Double value2() {
        return getWeight();
    }

    @Override
    public PathfinderNodegroupsRecord value1(NamespacedKey value) {
        setKey(value);
        return this;
    }

    @Override
    public PathfinderNodegroupsRecord value2(Double value) {
        setWeight(value);
        return this;
    }

    @Override
    public PathfinderNodegroupsRecord values(NamespacedKey value1, Double value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PathfinderNodegroupsRecord
     */
    public PathfinderNodegroupsRecord() {
        super(PathfinderNodegroups.PATHFINDER_NODEGROUPS);
    }

    /**
     * Create a detached, initialised PathfinderNodegroupsRecord
     */
    public PathfinderNodegroupsRecord(NamespacedKey key, Double weight) {
        super(PathfinderNodegroups.PATHFINDER_NODEGROUPS);

        setKey(key);
        setWeight(weight);
    }
}
