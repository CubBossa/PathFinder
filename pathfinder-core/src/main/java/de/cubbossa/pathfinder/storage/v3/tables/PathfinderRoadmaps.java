/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.storage.v3.tables;


import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter;
import de.cubbossa.pathfinder.storage.v3.DefaultSchema;
import de.cubbossa.pathfinder.storage.v3.Keys;
import de.cubbossa.pathfinder.storage.v3.tables.records.PathfinderRoadmapsRecord;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.function.Function;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PathfinderRoadmaps extends TableImpl<PathfinderRoadmapsRecord> {

  private static final long serialVersionUID = 1L;

  /**
   * The reference instance of <code>pathfinder_roadmaps</code>
   */
  public static final PathfinderRoadmaps PATHFINDER_ROADMAPS = new PathfinderRoadmaps();

  /**
   * The class holding records for this type
   */
  @Override
  public Class<PathfinderRoadmapsRecord> getRecordType() {
    return PathfinderRoadmapsRecord.class;
  }

  /**
   * The column <code>pathfinder_roadmaps.key</code>.
   */
  public final TableField<PathfinderRoadmapsRecord, NamespacedKey> KEY = createField(DSL.name("key"), SQLDataType.VARCHAR(64).nullable(false), this, "", new NamespacedKeyConverter());

  /**
   * The column <code>pathfinder_roadmaps.name_format</code>.
   */
  public final TableField<PathfinderRoadmapsRecord, String> NAME_FORMAT = createField(DSL.name("name_format"), SQLDataType.CLOB.nullable(false), this, "");

  /**
   * The column <code>pathfinder_roadmaps.path_visualizer</code>.
   */
  public final TableField<PathfinderRoadmapsRecord, String> PATH_VISUALIZER = createField(DSL.name("path_visualizer"), SQLDataType.VARCHAR(64), this, "");

  /**
   * The column <code>pathfinder_roadmaps.path_curve_length</code>.
   */
  public final TableField<PathfinderRoadmapsRecord, Double> PATH_CURVE_LENGTH = createField(DSL.name("path_curve_length"), SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("3", SQLDataType.DOUBLE)), this, "");

  private PathfinderRoadmaps(Name alias, Table<PathfinderRoadmapsRecord> aliased) {
    this(alias, aliased, null);
  }

  private PathfinderRoadmaps(Name alias, Table<PathfinderRoadmapsRecord> aliased, Field<?>[] parameters) {
    super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
  }

  /**
   * Create an aliased <code>pathfinder_roadmaps</code> table reference
   */
  public PathfinderRoadmaps(String alias) {
    this(DSL.name(alias), PATHFINDER_ROADMAPS);
  }

  /**
   * Create an aliased <code>pathfinder_roadmaps</code> table reference
   */
  public PathfinderRoadmaps(Name alias) {
    this(alias, PATHFINDER_ROADMAPS);
  }

  /**
   * Create a <code>pathfinder_roadmaps</code> table reference
   */
  public PathfinderRoadmaps() {
    this(DSL.name("pathfinder_roadmaps"), null);
  }

  public <O extends Record> PathfinderRoadmaps(Table<O> child, ForeignKey<O, PathfinderRoadmapsRecord> key) {
    super(child, key, PATHFINDER_ROADMAPS);
  }

  @Override
  public Schema getSchema() {
    return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
  }

  @Override
  public UniqueKey<PathfinderRoadmapsRecord> getPrimaryKey() {
    return Keys.PATHFINDER_ROADMAPS__PK_PATHFINDER_ROADMAPS;
  }

  @Override
  public PathfinderRoadmaps as(String alias) {
    return new PathfinderRoadmaps(DSL.name(alias), this);
  }

  @Override
  public PathfinderRoadmaps as(Name alias) {
    return new PathfinderRoadmaps(alias, this);
  }

  @Override
  public PathfinderRoadmaps as(Table<?> alias) {
    return new PathfinderRoadmaps(alias.getQualifiedName(), this);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderRoadmaps rename(String name) {
    return new PathfinderRoadmaps(DSL.name(name), null);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderRoadmaps rename(Name name) {
    return new PathfinderRoadmaps(name, null);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderRoadmaps rename(Table<?> name) {
    return new PathfinderRoadmaps(name.getQualifiedName(), null);
  }

  // -------------------------------------------------------------------------
  // Row4 type methods
  // -------------------------------------------------------------------------

  @Override
  public Row4<NamespacedKey, String, String, Double> fieldsRow() {
    return (Row4) super.fieldsRow();
  }

  /**
   * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
   */
  public <U> SelectField<U> mapping(Function4<? super NamespacedKey, ? super String, ? super String, ? super Double, ? extends U> from) {
    return convertFrom(Records.mapping(from));
  }

  /**
   * Convenience mapping calling {@link SelectField#convertFrom(Class,
   * Function)}.
   */
  public <U> SelectField<U> mapping(Class<U> toType, Function4<? super NamespacedKey, ? super String, ? super String, ? super Double, ? extends U> from) {
    return convertFrom(toType, Records.mapping(from));
  }
}
