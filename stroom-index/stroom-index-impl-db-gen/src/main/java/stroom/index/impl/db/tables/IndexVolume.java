/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import stroom.index.impl.db.Indexes;
import stroom.index.impl.db.Keys;
import stroom.index.impl.db.Stroom;
import stroom.index.impl.db.tables.records.IndexVolumeRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class IndexVolume extends TableImpl<IndexVolumeRecord> {

    private static final long serialVersionUID = 594856825;

    /**
     * The reference instance of <code>stroom.index_volume</code>
     */
    public static final IndexVolume INDEX_VOLUME = new IndexVolume();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IndexVolumeRecord> getRecordType() {
        return IndexVolumeRecord.class;
    }

    /**
     * The column <code>stroom.index_volume.id</code>.
     */
    public final TableField<IndexVolumeRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>stroom.index_volume.version</code>.
     */
    public final TableField<IndexVolumeRecord, Byte> VERSION = createField("version", org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>stroom.index_volume.created_by</code>.
     */
    public final TableField<IndexVolumeRecord, String> CREATED_BY = createField("created_by", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>stroom.index_volume.created_at</code>.
     */
    public final TableField<IndexVolumeRecord, Long> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.updated_by</code>.
     */
    public final TableField<IndexVolumeRecord, String> UPDATED_BY = createField("updated_by", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>stroom.index_volume.updated_at</code>.
     */
    public final TableField<IndexVolumeRecord, Long> UPDATED_AT = createField("updated_at", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.path</code>.
     */
    public final TableField<IndexVolumeRecord, String> PATH = createField("path", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.index_volume.index_status</code>.
     */
    public final TableField<IndexVolumeRecord, Byte> INDEX_STATUS = createField("index_status", org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>stroom.index_volume.volume_type</code>.
     */
    public final TableField<IndexVolumeRecord, Byte> VOLUME_TYPE = createField("volume_type", org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>stroom.index_volume.bytes_limit</code>.
     */
    public final TableField<IndexVolumeRecord, Long> BYTES_LIMIT = createField("bytes_limit", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.bytes_used</code>.
     */
    public final TableField<IndexVolumeRecord, Long> BYTES_USED = createField("bytes_used", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.bytes_free</code>.
     */
    public final TableField<IndexVolumeRecord, Long> BYTES_FREE = createField("bytes_free", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.bytes_total</code>.
     */
    public final TableField<IndexVolumeRecord, Long> BYTES_TOTAL = createField("bytes_total", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.status_ms</code>.
     */
    public final TableField<IndexVolumeRecord, Long> STATUS_MS = createField("status_ms", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_volume.index_uuid</code>.
     */
    public final TableField<IndexVolumeRecord, String> INDEX_UUID = createField("index_uuid", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>stroom.index_volume.node_name</code>.
     */
    public final TableField<IndexVolumeRecord, String> NODE_NAME = createField("node_name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * Create a <code>stroom.index_volume</code> table reference
     */
    public IndexVolume() {
        this(DSL.name("index_volume"), null);
    }

    /**
     * Create an aliased <code>stroom.index_volume</code> table reference
     */
    public IndexVolume(String alias) {
        this(DSL.name(alias), INDEX_VOLUME);
    }

    /**
     * Create an aliased <code>stroom.index_volume</code> table reference
     */
    public IndexVolume(Name alias) {
        this(alias, INDEX_VOLUME);
    }

    private IndexVolume(Name alias, Table<IndexVolumeRecord> aliased) {
        this(alias, aliased, null);
    }

    private IndexVolume(Name alias, Table<IndexVolumeRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> IndexVolume(Table<O> child, ForeignKey<O, IndexVolumeRecord> key) {
        super(child, key, INDEX_VOLUME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Stroom.STROOM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.INDEX_VOLUME_NODE_NAME_PATH, Indexes.INDEX_VOLUME_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<IndexVolumeRecord, Integer> getIdentity() {
        return Keys.IDENTITY_INDEX_VOLUME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<IndexVolumeRecord> getPrimaryKey() {
        return Keys.KEY_INDEX_VOLUME_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<IndexVolumeRecord>> getKeys() {
        return Arrays.<UniqueKey<IndexVolumeRecord>>asList(Keys.KEY_INDEX_VOLUME_PRIMARY, Keys.KEY_INDEX_VOLUME_NODE_NAME_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableField<IndexVolumeRecord, Byte> getRecordVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolume as(String alias) {
        return new IndexVolume(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexVolume as(Name alias) {
        return new IndexVolume(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public IndexVolume rename(String name) {
        return new IndexVolume(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IndexVolume rename(Name name) {
        return new IndexVolume(name, null);
    }
}
