/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq.tables;


import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.jooq.DefaultSchema;
import de.cubbossa.pathfinder.jooq.Keys;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter;

import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function2;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PathfinderNodegroups extends TableImpl<PathfinderNodegroupsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>pathfinder_nodegroups</code>
     */
    public static final PathfinderNodegroups PATHFINDER_NODEGROUPS = new PathfinderNodegroups();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PathfinderNodegroupsRecord> getRecordType() {
        return PathfinderNodegroupsRecord.class;
    }

    /**
     * The column <code>pathfinder_nodegroups.key</code>.
     */
    public final TableField<PathfinderNodegroupsRecord, NamespacedKey> KEY = createField(DSL.name("key"), SQLDataType.VARCHAR(64).nullable(false), this, "", new NamespacedKeyConverter());

    /**
     * The column <code>pathfinder_nodegroups.weight</code>.
     */
    public final TableField<PathfinderNodegroupsRecord, Double> WEIGHT = createField(DSL.name("weight"), SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("1", SQLDataType.DOUBLE)), this, "");

    private PathfinderNodegroups(Name alias, Table<PathfinderNodegroupsRecord> aliased) {
        this(alias, aliased, null);
    }

    private PathfinderNodegroups(Name alias, Table<PathfinderNodegroupsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>pathfinder_nodegroups</code> table reference
     */
    public PathfinderNodegroups(String alias) {
        this(DSL.name(alias), PATHFINDER_NODEGROUPS);
    }

    /**
     * Create an aliased <code>pathfinder_nodegroups</code> table reference
     */
    public PathfinderNodegroups(Name alias) {
        this(alias, PATHFINDER_NODEGROUPS);
    }

    /**
     * Create a <code>pathfinder_nodegroups</code> table reference
     */
    public PathfinderNodegroups() {
        this(DSL.name("pathfinder_nodegroups"), null);
    }

    public <O extends Record> PathfinderNodegroups(Table<O> child, ForeignKey<O, PathfinderNodegroupsRecord> key) {
        super(child, key, PATHFINDER_NODEGROUPS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<PathfinderNodegroupsRecord> getPrimaryKey() {
        return Keys.PATHFINDER_NODEGROUPS__PATHFINDER_NODEGROUPS__PK;
    }

    @Override
    public PathfinderNodegroups as(String alias) {
        return new PathfinderNodegroups(DSL.name(alias), this);
    }

    @Override
    public PathfinderNodegroups as(Name alias) {
        return new PathfinderNodegroups(alias, this);
    }

    @Override
    public PathfinderNodegroups as(Table<?> alias) {
        return new PathfinderNodegroups(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public PathfinderNodegroups rename(String name) {
        return new PathfinderNodegroups(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public PathfinderNodegroups rename(Name name) {
        return new PathfinderNodegroups(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public PathfinderNodegroups rename(Table<?> name) {
        return new PathfinderNodegroups(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<NamespacedKey, Double> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function2<? super NamespacedKey, ? super Double, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function2<? super NamespacedKey, ? super Double, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
