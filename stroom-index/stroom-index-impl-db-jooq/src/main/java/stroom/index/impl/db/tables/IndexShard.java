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
import stroom.index.impl.db.tables.records.IndexShardRecord;


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
public class IndexShard extends TableImpl<IndexShardRecord> {

    private static final long serialVersionUID = 903439591;

    /**
     * The reference instance of <code>stroom.index_shard</code>
     */
    public static final IndexShard INDEX_SHARD = new IndexShard();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IndexShardRecord> getRecordType() {
        return IndexShardRecord.class;
    }

    /**
     * The column <code>stroom.index_shard.id</code>.
     */
    public final TableField<IndexShardRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>stroom.index_shard.node_name</code>.
     */
    public final TableField<IndexShardRecord, String> NODE_NAME = createField("node_name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.index_shard.fk_volume_id</code>.
     */
    public final TableField<IndexShardRecord, Long> FK_VOLUME_ID = createField("fk_volume_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>stroom.index_shard.index_uuid</code>.
     */
    public final TableField<IndexShardRecord, String> INDEX_UUID = createField("index_uuid", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.index_shard.commit_document_count</code>.
     */
    public final TableField<IndexShardRecord, Integer> COMMIT_DOCUMENT_COUNT = createField("commit_document_count", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>stroom.index_shard.commit_duration_ms</code>.
     */
    public final TableField<IndexShardRecord, Long> COMMIT_DURATION_MS = createField("commit_duration_ms", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_shard.commit_ms</code>.
     */
    public final TableField<IndexShardRecord, Long> COMMIT_MS = createField("commit_ms", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_shard.document_count</code>.
     */
    public final TableField<IndexShardRecord, Integer> DOCUMENT_COUNT = createField("document_count", org.jooq.impl.SQLDataType.INTEGER.defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>stroom.index_shard.file_size</code>.
     */
    public final TableField<IndexShardRecord, Long> FILE_SIZE = createField("file_size", org.jooq.impl.SQLDataType.BIGINT.defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>stroom.index_shard.status</code>.
     */
    public final TableField<IndexShardRecord, Byte> STATUS = createField("status", org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>stroom.index_shard.partition</code>.
     */
    public final TableField<IndexShardRecord, String> PARTITION = createField("partition", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>stroom.index_shard.index_version</code>.
     */
    public final TableField<IndexShardRecord, String> INDEX_VERSION = createField("index_version", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>stroom.index_shard.partition_from_ms</code>.
     */
    public final TableField<IndexShardRecord, Long> PARTITION_FROM_MS = createField("partition_from_ms", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>stroom.index_shard.partition_to_ms</code>.
     */
    public final TableField<IndexShardRecord, Long> PARTITION_TO_MS = createField("partition_to_ms", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>stroom.index_shard</code> table reference
     */
    public IndexShard() {
        this(DSL.name("index_shard"), null);
    }

    /**
     * Create an aliased <code>stroom.index_shard</code> table reference
     */
    public IndexShard(String alias) {
        this(DSL.name(alias), INDEX_SHARD);
    }

    /**
     * Create an aliased <code>stroom.index_shard</code> table reference
     */
    public IndexShard(Name alias) {
        this(alias, INDEX_SHARD);
    }

    private IndexShard(Name alias, Table<IndexShardRecord> aliased) {
        this(alias, aliased, null);
    }

    private IndexShard(Name alias, Table<IndexShardRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> IndexShard(Table<O> child, ForeignKey<O, IndexShardRecord> key) {
        super(child, key, INDEX_SHARD);
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
        return Arrays.<Index>asList(Indexes.INDEX_SHARD_INDEX_SHARD_FK_VOLUME_ID, Indexes.INDEX_SHARD_INDEX_SHARD_INDEX_UUID, Indexes.INDEX_SHARD_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<IndexShardRecord, Long> getIdentity() {
        return Keys.IDENTITY_INDEX_SHARD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<IndexShardRecord> getPrimaryKey() {
        return Keys.KEY_INDEX_SHARD_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<IndexShardRecord>> getKeys() {
        return Arrays.<UniqueKey<IndexShardRecord>>asList(Keys.KEY_INDEX_SHARD_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<IndexShardRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<IndexShardRecord, ?>>asList(Keys.INDEX_SHARD_FK_VOLUME_ID);
    }

    public IndexVolume indexVolume() {
        return new IndexVolume(this, Keys.INDEX_SHARD_FK_VOLUME_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexShard as(String alias) {
        return new IndexShard(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexShard as(Name alias) {
        return new IndexShard(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public IndexShard rename(String name) {
        return new IndexShard(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IndexShard rename(Name name) {
        return new IndexShard(name, null);
    }
}
