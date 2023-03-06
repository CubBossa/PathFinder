/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq.tables.records;


import de.cubbossa.pathfinder.jooq.tables.PathfinderNodes;

import org.bukkit.NamespacedKey;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PathfinderNodesRecord extends UpdatableRecordImpl<PathfinderNodesRecord> implements Record8<Integer, NamespacedKey, NamespacedKey, Double, Double, Double, String, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>pathfinder_nodes.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>pathfinder_nodes.type</code>.
     */
    public void setType(NamespacedKey value) {
        set(1, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.type</code>.
     */
    public NamespacedKey getType() {
        return (NamespacedKey) get(1);
    }

    /**
     * Setter for <code>pathfinder_nodes.roadmap_key</code>.
     */
    public void setRoadmapKey(NamespacedKey value) {
        set(2, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.roadmap_key</code>.
     */
    public NamespacedKey getRoadmapKey() {
        return (NamespacedKey) get(2);
    }

    /**
     * Setter for <code>pathfinder_nodes.x</code>.
     */
    public void setX(Double value) {
        set(3, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.x</code>.
     */
    public Double getX() {
        return (Double) get(3);
    }

    /**
     * Setter for <code>pathfinder_nodes.y</code>.
     */
    public void setY(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.y</code>.
     */
    public Double getY() {
        return (Double) get(4);
    }

    /**
     * Setter for <code>pathfinder_nodes.z</code>.
     */
    public void setZ(Double value) {
        set(5, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.z</code>.
     */
    public Double getZ() {
        return (Double) get(5);
    }

    /**
     * Setter for <code>pathfinder_nodes.world</code>.
     */
    public void setWorld(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.world</code>.
     */
    public String getWorld() {
        return (String) get(6);
    }

    /**
     * Setter for <code>pathfinder_nodes.path_curve_length</code>.
     */
    public void setPathCurveLength(Double value) {
        set(7, value);
    }

    /**
     * Getter for <code>pathfinder_nodes.path_curve_length</code>.
     */
    public Double getPathCurveLength() {
        return (Double) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<Integer, NamespacedKey, NamespacedKey, Double, Double, Double, String, Double> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<Integer, NamespacedKey, NamespacedKey, Double, Double, Double, String, Double> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return PathfinderNodes.PATHFINDER_NODES.ID;
    }

    @Override
    public Field<NamespacedKey> field2() {
        return PathfinderNodes.PATHFINDER_NODES.TYPE;
    }

    @Override
    public Field<NamespacedKey> field3() {
        return PathfinderNodes.PATHFINDER_NODES.ROADMAP_KEY;
    }

    @Override
    public Field<Double> field4() {
        return PathfinderNodes.PATHFINDER_NODES.X;
    }

    @Override
    public Field<Double> field5() {
        return PathfinderNodes.PATHFINDER_NODES.Y;
    }

    @Override
    public Field<Double> field6() {
        return PathfinderNodes.PATHFINDER_NODES.Z;
    }

    @Override
    public Field<String> field7() {
        return PathfinderNodes.PATHFINDER_NODES.WORLD;
    }

    @Override
    public Field<Double> field8() {
        return PathfinderNodes.PATHFINDER_NODES.PATH_CURVE_LENGTH;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public NamespacedKey component2() {
        return getType();
    }

    @Override
    public NamespacedKey component3() {
        return getRoadmapKey();
    }

    @Override
    public Double component4() {
        return getX();
    }

    @Override
    public Double component5() {
        return getY();
    }

    @Override
    public Double component6() {
        return getZ();
    }

    @Override
    public String component7() {
        return getWorld();
    }

    @Override
    public Double component8() {
        return getPathCurveLength();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public NamespacedKey value2() {
        return getType();
    }

    @Override
    public NamespacedKey value3() {
        return getRoadmapKey();
    }

    @Override
    public Double value4() {
        return getX();
    }

    @Override
    public Double value5() {
        return getY();
    }

    @Override
    public Double value6() {
        return getZ();
    }

    @Override
    public String value7() {
        return getWorld();
    }

    @Override
    public Double value8() {
        return getPathCurveLength();
    }

    @Override
    public PathfinderNodesRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value2(NamespacedKey value) {
        setType(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value3(NamespacedKey value) {
        setRoadmapKey(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value4(Double value) {
        setX(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value5(Double value) {
        setY(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value6(Double value) {
        setZ(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value7(String value) {
        setWorld(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord value8(Double value) {
        setPathCurveLength(value);
        return this;
    }

    @Override
    public PathfinderNodesRecord values(Integer value1, NamespacedKey value2, NamespacedKey value3, Double value4, Double value5, Double value6, String value7, Double value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PathfinderNodesRecord
     */
    public PathfinderNodesRecord() {
        super(PathfinderNodes.PATHFINDER_NODES);
    }

    /**
     * Create a detached, initialised PathfinderNodesRecord
     */
    public PathfinderNodesRecord(Integer id, NamespacedKey type, NamespacedKey roadmapKey, Double x, Double y, Double z, String world, Double pathCurveLength) {
        super(PathfinderNodes.PATHFINDER_NODES);

        setId(id);
        setType(type);
        setRoadmapKey(roadmapKey);
        setX(x);
        setY(y);
        setZ(z);
        setWorld(world);
        setPathCurveLength(pathCurveLength);
    }
}
