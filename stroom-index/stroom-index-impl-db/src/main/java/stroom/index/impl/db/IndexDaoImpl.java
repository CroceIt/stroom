package stroom.index.impl.db;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.index.dao.IndexDao;

import javax.inject.Inject;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class IndexDaoImpl implements IndexDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexDaoImpl.class);

    private final ConnectionProvider connectionProvider;

    // Stroom User table
    private static final Table<Record> TABLE_STROOM_USER = table("stroom_user");
    private static final Field<Long> FIELD_ID = field("id", Long.class);

    @Inject
    public IndexDaoImpl(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

}
