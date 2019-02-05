/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package stroom.stats;


import org.junit.jupiter.api.Test;
import stroom.docref.DocRef;
import stroom.entity.shared.DocRefs;
import stroom.explorer.api.ExplorerService;
import stroom.importexport.impl.ImportExportSerializer;
import stroom.importexport.shared.ImportState;
import stroom.statistics.shared.StatisticStore;
import stroom.statistics.shared.StatisticType;
import stroom.statistics.sql.entity.StatisticsDataSourceProvider;
import stroom.statistics.stroomstats.entity.StroomStatsStoreStore;
import stroom.stats.shared.StatisticField;
import stroom.stats.shared.StroomStatsStoreDoc;
import stroom.stats.shared.StroomStatsStoreEntityData;
import stroom.test.AbstractCoreIntegrationTest;
import stroom.util.io.FileUtil;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestStroomStatsStoreImportExportSerializer extends AbstractCoreIntegrationTest {
    @Inject
    private ImportExportSerializer importExportSerializer;
    @Inject
    private StroomStatsStoreStore stroomStatsStoreStore;
    @Inject
    private StatisticsDataSourceProvider statisticsDataSourceProvider;
    @Inject
    private ExplorerService explorerService;

    private DocRefs buildFindFolderCriteria(DocRef folderDocRef) {
        final DocRefs docRefs = new DocRefs();
        docRefs.add(folderDocRef);
        return docRefs;
    }

    /**
     * Create a populated {@link StatisticStore} object, serialise it to file,
     * de-serialise it back to an object then compare the first object with the
     * second one
     */
    @Test
    void testStatisticsDataSource() {
        final DocRef docRef = explorerService.create(StroomStatsStoreDoc.DOCUMENT_TYPE, "StatName1", null, null);
        final StroomStatsStoreDoc entity = stroomStatsStoreStore.readDocument(docRef);
        entity.setDescription("My Description");
        entity.setStatisticType(StatisticType.COUNT);
        entity.setConfig(new StroomStatsStoreEntityData());
        entity.getConfig().addStatisticField(new StatisticField("tag1"));
        entity.getConfig().addStatisticField(new StatisticField("tag2"));
        stroomStatsStoreStore.writeDocument(entity);

        assertThat(stroomStatsStoreStore.list().size()).isEqualTo(1);

        final Path testDataDir = getCurrentTestDir().resolve("ExportTest");

        FileUtil.deleteDir(testDataDir);
        FileUtil.mkdirs(testDataDir);

        final DocRefs docRefs = new DocRefs();
        docRefs.add(docRef);
        importExportSerializer.write(testDataDir, docRefs, true, null);

        assertThat(FileUtil.count(testDataDir)).isEqualTo(2);

        // now clear out the java entities and import from file
        clean(true);

        assertThat(stroomStatsStoreStore.list().size()).isEqualTo(0);

        importExportSerializer.read(testDataDir, null, ImportState.ImportMode.IGNORE_CONFIRMATION);

        final List<DocRef> dataSources = stroomStatsStoreStore.list();

        assertThat(dataSources.size()).isEqualTo(1);

        final StroomStatsStoreDoc importedDataSource = stroomStatsStoreStore.readDocument(dataSources.get(0));

        assertThat(importedDataSource.getName()).isEqualTo(entity.getName());
        assertThat(importedDataSource.getStatisticType()).isEqualTo(entity.getStatisticType());
        assertThat(importedDataSource.getDescription()).isEqualTo(entity.getDescription());

        assertThat(importedDataSource.getConfig()).isEqualTo(entity.getConfig());
    }
}