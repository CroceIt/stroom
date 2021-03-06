package stroom.meta.impl.db;

import stroom.cache.api.CacheManager;
import stroom.db.util.AbstractFlyWayDbModule;
import stroom.db.util.DataSourceProxy;
import stroom.lifecycle.api.LifecycleBinder;
import stroom.meta.impl.MetaDao;
import stroom.meta.impl.MetaFeedDao;
import stroom.meta.impl.MetaKeyDao;
import stroom.meta.impl.MetaProcessorDao;
import stroom.meta.impl.MetaRetentionTrackerDao;
import stroom.meta.impl.MetaTypeDao;
import stroom.meta.impl.MetaValueDao;
import stroom.util.RunnableWrapper;
import stroom.util.guice.GuiceUtil;
import stroom.util.shared.Clearable;

import javax.inject.Inject;
import javax.sql.DataSource;

public class MetaDbModule extends AbstractFlyWayDbModule<MetaServiceConfig, MetaDbConnProvider> {
    private static final String MODULE = "stroom-meta";
    private static final String FLYWAY_LOCATIONS = "stroom/meta/impl/db/migration";
    private static final String FLYWAY_TABLE = "meta_schema_history";

    @Override
    protected void configure() {
        super.configure();

        requireBinding(CacheManager.class);

        bind(MetaFeedDao.class).to(MetaFeedDaoImpl.class);
        bind(MetaTypeDao.class).to(MetaTypeDaoImpl.class);
        bind(MetaProcessorDao.class).to(MetaProcessorDaoImpl.class);
        bind(MetaKeyDao.class).to(MetaKeyDaoImpl.class);
        bind(MetaValueDao.class).to(MetaValueDaoImpl.class);
        bind(MetaDao.class).to(MetaDaoImpl.class);
        bind(MetaRetentionTrackerDao.class).to(MetaRetentionTrackerDaoImpl.class);

        GuiceUtil.buildMultiBinder(binder(), Clearable.class)
                .addBinding(Cleanup.class);

        LifecycleBinder.create(binder())
                .bindShutdownTaskTo(MetaValueServiceFlush.class);
    }

    @Override
    protected String getFlyWayTableName() {
        return FLYWAY_TABLE;
    }

    @Override
    protected String getModuleName() {
        return MODULE;
    }

    @Override
    protected String getFlyWayLocation() {
        return FLYWAY_LOCATIONS;
    }

    @Override
    protected Class<MetaDbConnProvider> getConnectionProviderType() {
        return MetaDbConnProvider.class;
    }

    @Override
    protected MetaDbConnProvider createConnectionProvider(final DataSource dataSource) {
        return new DataSourceImpl(dataSource);
    }

    private static class DataSourceImpl extends DataSourceProxy implements MetaDbConnProvider {
        private DataSourceImpl(final DataSource dataSource) {
            super(dataSource);
        }
    }

    private static class MetaValueServiceFlush extends RunnableWrapper {
        @Inject
        MetaValueServiceFlush(final MetaValueDaoImpl metaValueService) {
            super(metaValueService::flush);
        }
    }
}
