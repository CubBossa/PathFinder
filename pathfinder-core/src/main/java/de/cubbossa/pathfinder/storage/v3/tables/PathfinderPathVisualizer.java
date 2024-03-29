/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.storage.v3.tables;


import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter;
import de.cubbossa.pathfinder.storage.v3.DefaultSchema;
import de.cubbossa.pathfinder.storage.v3.Keys;
import de.cubbossa.pathfinder.storage.v3.tables.records.PathfinderPathVisualizerRecord;
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
public class PathfinderPathVisualizer extends TableImpl<PathfinderPathVisualizerRecord> {

  private static final long serialVersionUID = 1L;

  /**
   * The reference instance of <code>pathfinder_path_visualizer</code>
   */
  public static final PathfinderPathVisualizer PATHFINDER_PATH_VISUALIZER = new PathfinderPathVisualizer();

  /**
   * The class holding records for this type
   */
  @Override
  public Class<PathfinderPathVisualizerRecord> getRecordType() {
    return PathfinderPathVisualizerRecord.class;
  }

  /**
   * The column <code>pathfinder_path_visualizer.key</code>.
   */
  public final TableField<PathfinderPathVisualizerRecord, NamespacedKey> KEY = createField(DSL.name("key"), SQLDataType.VARCHAR.nullable(false), this, "", new NamespacedKeyConverter());

  /**
   * The column <code>pathfinder_path_visualizer.type</code>.
   */
  public final TableField<PathfinderPathVisualizerRecord, NamespacedKey> TYPE = createField(DSL.name("type"), SQLDataType.VARCHAR.nullable(false), this, "", new NamespacedKeyConverter());

  /**
   * The column <code>pathfinder_path_visualizer.name_format</code>.
   */
  public final TableField<PathfinderPathVisualizerRecord, String> NAME_FORMAT = createField(DSL.name("name_format"), SQLDataType.VARCHAR.nullable(false), this, "");

  /**
   * The column <code>pathfinder_path_visualizer.permission</code>.
   */
  public final TableField<PathfinderPathVisualizerRecord, String> PERMISSION = createField(DSL.name("permission"), SQLDataType.VARCHAR(64), this, "");

  /**
   * The column <code>pathfinder_path_visualizer.interval</code>.
   */
  public final TableField<PathfinderPathVisualizerRecord, Integer> INTERVAL = createField(DSL.name("interval"), SQLDataType.INTEGER.nullable(false), this, "");

  /**
   * The column <code>pathfinder_path_visualizer.data</code>.
   */
  public final TableField<PathfinderPathVisualizerRecord, String> DATA = createField(DSL.name("data"), SQLDataType.VARCHAR, this, "");

  private PathfinderPathVisualizer(Name alias, Table<PathfinderPathVisualizerRecord> aliased) {
    this(alias, aliased, null);
  }

  private PathfinderPathVisualizer(Name alias, Table<PathfinderPathVisualizerRecord> aliased, Field<?>[] parameters) {
    super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
  }

  /**
   * Create an aliased <code>pathfinder_path_visualizer</code> table reference
   */
  public PathfinderPathVisualizer(String alias) {
    this(DSL.name(alias), PATHFINDER_PATH_VISUALIZER);
  }

  /**
   * Create an aliased <code>pathfinder_path_visualizer</code> table reference
   */
  public PathfinderPathVisualizer(Name alias) {
    this(alias, PATHFINDER_PATH_VISUALIZER);
  }

  /**
   * Create a <code>pathfinder_path_visualizer</code> table reference
   */
  public PathfinderPathVisualizer() {
    this(DSL.name("pathfinder_path_visualizer"), null);
  }

  public <O extends Record> PathfinderPathVisualizer(Table<O> child, ForeignKey<O, PathfinderPathVisualizerRecord> key) {
    super(child, key, PATHFINDER_PATH_VISUALIZER);
  }

  @Override
  public Schema getSchema() {
    return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
  }

  @Override
  public UniqueKey<PathfinderPathVisualizerRecord> getPrimaryKey() {
    return Keys.PATHFINDER_PATH_VISUALIZER__PK_PATHFINDER_PATH_VISUALIZER;
  }

  @Override
  public PathfinderPathVisualizer as(String alias) {
    return new PathfinderPathVisualizer(DSL.name(alias), this);
  }

  @Override
  public PathfinderPathVisualizer as(Name alias) {
    return new PathfinderPathVisualizer(alias, this);
  }

  @Override
  public PathfinderPathVisualizer as(Table<?> alias) {
    return new PathfinderPathVisualizer(alias.getQualifiedName(), this);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderPathVisualizer rename(String name) {
    return new PathfinderPathVisualizer(DSL.name(name), null);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderPathVisualizer rename(Name name) {
    return new PathfinderPathVisualizer(name, null);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderPathVisualizer rename(Table<?> name) {
    return new PathfinderPathVisualizer(name.getQualifiedName(), null);
  }

  // -------------------------------------------------------------------------
  // Row6 type methods
  // -------------------------------------------------------------------------

  @Override
  public Row6<NamespacedKey, NamespacedKey, String, String, Integer, String> fieldsRow() {
    return (Row6) super.fieldsRow();
  }

  /**
   * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
   */
  public <U> SelectField<U> mapping(Function6<? super NamespacedKey, ? super NamespacedKey, ? super String, ? super String, ? super Integer, ? super String, ? extends U> from) {
    return convertFrom(Records.mapping(from));
  }

  /**
   * Convenience mapping calling {@link SelectField#convertFrom(Class,
   * Function)}.
   */
  public <U> SelectField<U> mapping(Class<U> toType, Function6<? super NamespacedKey, ? super NamespacedKey, ? super String, ? super String, ? super Integer, ? super String, ? extends U> from) {
    return convertFrom(toType, Records.mapping(from));
  }
}
