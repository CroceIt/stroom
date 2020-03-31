/*
 * Copyright 2020 Crown Copyright
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
package stroom.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.docref.DocRef;
import stroom.docstore.api.AuditFieldFilter;
import stroom.docstore.api.Serialiser2;
import stroom.docstore.api.Serialiser2Factory;
import stroom.entity.shared.ExpressionCriteria;
import stroom.importexport.api.ImportExportActionHandler;
import stroom.importexport.api.ImportExportDocumentEventLog;
import stroom.importexport.api.NonExplorerDocRefProvider;
import stroom.importexport.shared.ImportState;
import stroom.pipeline.shared.PipelineDoc;
import stroom.pipeline.shared.data.PipelineData;
import stroom.processor.api.ProcessorFilterService;
import stroom.processor.api.ProcessorService;
import stroom.processor.shared.Processor;
import stroom.processor.shared.ProcessorDataSource;
import stroom.processor.shared.ProcessorFilter;
import stroom.processor.shared.ProcessorFilterDataSource;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.ExpressionTerm;
import stroom.util.shared.Message;
import stroom.util.shared.ResultPage;
import stroom.util.xml.XMLMarshallerUtil;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProcessorFilterImportExportHandlerImpl implements ImportExportActionHandler, NonExplorerDocRefProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorImportExportHandlerImpl.class);
    private final ImportExportDocumentEventLog importExportDocumentEventLog;
    private final ProcessorFilterService processorFilterService;
    private final ProcessorService processorService;

    private static final String XML = "xml";
    private static final String META = "meta";

    private final Serialiser2<ProcessorFilter> delegate;

    @Inject
    ProcessorFilterImportExportHandlerImpl(final ProcessorFilterService processorFilterService, final ProcessorService processorService, final ImportExportDocumentEventLog importExportDocumentEventLog, final Serialiser2Factory serialiser2Factory){
        this.processorFilterService = processorFilterService;
        this.processorService = processorService;
        this.importExportDocumentEventLog = importExportDocumentEventLog;
        this.delegate = serialiser2Factory.createSerialiser(ProcessorFilter.class);
    }

    @Override
    public DocRef importDocument(DocRef docRef, Map<String, byte[]> dataMap, ImportState importState, ImportState.ImportMode importMode) {
        if (dataMap.get(META) == null)
            throw new IllegalArgumentException("Unable to import Processor with no meta file.  Docref is " + docRef);

        final ProcessorFilter processorFilter;
        try{
            processorFilter = delegate.read(dataMap.get(META));
        } catch (IOException ex){
            throw new RuntimeException("Unable to read meta file associated with processor " + docRef, ex);
        }

        processorFilter.setProcessor(findProcessorForFilter(processorFilter));
        if (ImportState.State.NEW.equals(importState.getState())) {
            Processor processor = processorService.create(
                    new DocRef(PipelineDoc.DOCUMENT_TYPE, processorFilter.getProcessorUuid()),
                    new DocRef(PipelineDoc.DOCUMENT_TYPE,processorFilter.getPipelineUuid()),
                    false);
            processorFilterService.create(processor,new DocRef(ProcessorFilter.ENTITY_TYPE,processorFilter.getUuid(),null),
                    processorFilter.getQueryData(),
                    processorFilter.getPriority(), processorFilter.isEnabled());
        } else if (ImportState.State.UPDATE.equals(importState.getState())) {
            ProcessorFilter currentVersion = findProcessorFilter(docRef);
            if (currentVersion != null) {
                processorFilter.setId(currentVersion.getId());
            }
            processorFilterService.update(processorFilter);
        }

        return docRef;
    }

    private ProcessorFilter findProcessorFilter (DocRef docRef){
        if (docRef == null || docRef.getUuid() == null)
            return null;

        final ExpressionOperator expression = new ExpressionOperator.Builder()
                .addTerm(ProcessorFilterDataSource.UUID, ExpressionTerm.Condition.EQUALS, docRef.getUuid()).build();

        ExpressionCriteria criteria = new ExpressionCriteria(expression);
        ResultPage<ProcessorFilter> page = processorFilterService.find(criteria);

        RuntimeException ex = null;
        if (page.size() == 0)
            ex = new RuntimeException("Processor filter with DocRef " + docRef + " not found.");

        if (page.size() > 1)
            ex = new IllegalStateException("Multiple processor filters with DocRef " + docRef + " found.");


        final ProcessorFilter processorFilter = page.getFirst();

        return processorFilter;
    }

    @Override
    public Map<String, byte[]> exportDocument(DocRef docRef, boolean omitAuditFields, List<Message> messageList) {
        if (docRef == null)
            return null;

        //Don't export certain fields
        ProcessorFilter processorFilter = new AuditFieldFilter<ProcessorFilter>().apply(findProcessorFilter(docRef));
        //Try to ensure that the processor uuid field is set
//        processorFilter.setProcessorUuid(processorFilter.getProcessor());
        processorFilter.setId(null);
        processorFilter.setVersion(null);
        processorFilter.setProcessorFilterTracker(null);
        processorFilter.setProcessor(null);
        processorFilter.setData(null);

        Map<String, byte[]> data;
        try {
            data = delegate.write(processorFilter);
        }catch (IOException ioex){
            LOGGER.error ("Unable to create meta file for processor filter", ioex);
            importExportDocumentEventLog.exportDocument(docRef, ioex);
            throw new RuntimeException("Unable to create meta file for processor filter", ioex);
        }

        importExportDocumentEventLog.exportDocument(docRef, null);

        return data;
    }


    @Override
    public Set<DocRef> listDocuments() {
        return null;
    }

    @Override
    public Map<DocRef, Set<DocRef>> getDependencies() {
        return null;
    }

    @Override
    public String getType() {
        return ProcessorFilter.ENTITY_TYPE;
    }

    @Override
    public DocRef findNearestExplorerDocRef(DocRef docref) {
        if (docref != null && ProcessorFilter.ENTITY_TYPE.equals(docref.getType())){
            ProcessorFilter processorFilter = findProcessorFilter(docref);

            Processor processor = findProcessorForFilter (processorFilter);

            DocRef pipelineDocRef = new DocRef(PipelineDoc.DOCUMENT_TYPE, processor.getPipelineUuid());

            return pipelineDocRef;
        }

        return null;
    }

    @Override
    public String findNameOfDocRef(DocRef docRef) {
        if (docRef == null)
            return "Processor Filter Null";
        return "Processor Filter " + docRef.getUuid().substring(0,7);
    }

    private Processor findProcessorForFilter (ProcessorFilter filter){
        Processor processor = filter.getProcessor();
        if (processor == null) {
            processor = findProcessor(filter.getUuid(), filter.getProcessorUuid(), filter.getPipelineUuid());
            filter.setProcessor(processor);
        }

        return processor;
    }

    private Processor findProcessor (String uuid, String processorUuid, String pipelineUuid){
        if (uuid == null)
            return null;

        final ExpressionOperator expression = new ExpressionOperator.Builder()
                .addTerm(ProcessorDataSource.UUID, ExpressionTerm.Condition.EQUALS, uuid).build();

        ExpressionCriteria criteria = new ExpressionCriteria(expression);
        ResultPage<Processor> page = processorService.find(criteria);

        RuntimeException ex = null;
        if (page.size() == 0){
            if (pipelineUuid != null) {
                //Create the missing processor
                processorService.create(new DocRef(Processor.ENTITY_TYPE, processorUuid), new DocRef(PipelineDoc.DOCUMENT_TYPE,pipelineUuid), true);
            } else {
                throw new RuntimeException("Unable to find processor for filter " + uuid);
            }
        }

        if (page.size() > 1)
            ex = new IllegalStateException("Multiple processors with DocRef " + uuid + " found.");

        if (ex != null) {
            LOGGER.error("Unable to export processor", ex);
            throw ex;
        }
        final Processor processor = page.getFirst();

        return processor;
    }
}
