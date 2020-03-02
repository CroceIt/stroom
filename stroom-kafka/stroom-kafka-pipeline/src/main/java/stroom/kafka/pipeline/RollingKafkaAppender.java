package stroom.kafka.pipeline;

import stroom.docref.DocRef;
import stroom.kafkaConfig.shared.KafkaConfigDoc;
import stroom.pipeline.destination.RollingDestination;
import stroom.pipeline.destination.RollingDestinations;
import stroom.pipeline.errorhandler.ProcessException;
import stroom.pipeline.factory.ConfigurableElement;
import stroom.pipeline.factory.PipelineProperty;
import stroom.pipeline.factory.PipelinePropertyDocRef;
import stroom.pipeline.shared.ElementIcons;
import stroom.pipeline.shared.data.PipelineElementType;
import stroom.pipeline.writer.AbstractRollingAppender;
import stroom.pipeline.writer.PathCreator;
import stroom.task.api.TaskContext;

import javax.inject.Inject;

@ConfigurableElement(
        type = "RollingKafkaAppender",
        category = PipelineElementType.Category.DESTINATION,
        roles = {
                PipelineElementType.ROLE_TARGET,
                PipelineElementType.ROLE_DESTINATION,
                PipelineElementType.VISABILITY_STEPPING},
        icon = ElementIcons.KAFKA)
class RollingKafkaAppender extends AbstractRollingAppender {
    private final stroom.kafkanew.pipeline.KafkaProducerFactory stroomKafkaProducerFactory;
    private final PathCreator pathCreator;

    private String topic;
    private String recordKey;
    private boolean flushOnSend = true;
    private DocRef kafkaConfigRef;

    private String key;

    @Inject
    RollingKafkaAppender(final RollingDestinations destinations,
                         final TaskContext taskContext,
                         final stroom.kafkanew.pipeline.KafkaProducerFactory stroomKafkaProducerFactory,
                         final PathCreator pathCreator) {
        super(destinations, taskContext);
        this.stroomKafkaProducerFactory = stroomKafkaProducerFactory;
        this.pathCreator = pathCreator;
    }

    @Override
    protected void validateSpecificSettings() {
        if (kafkaConfigRef == null) {
            throw new ProcessException("No Kafka config has been specified");
        }

        if (recordKey == null || recordKey.length() == 0) {
            throw new ProcessException("No recordKey has been specified");
        }

        if (topic == null || topic.length() == 0) {
            throw new ProcessException("No topic has been specified");
        }
    }

    @Override
    protected Object getKey() {
        if (key == null) {
            //this allows us to have two destinations for the same key and topic but with different
            //flush semantics
            key = String.format("%s:%s:%s", this.topic, this.recordKey, Boolean.toString(flushOnSend));
        }
        return key;
    }

    @Override
    public RollingDestination createDestination() {
        final org.apache.kafka.clients.producer.KafkaProducer stroomKafkaProducer = stroomKafkaProducerFactory.createProducer(kafkaConfigRef)
                .orElseThrow(() -> new ProcessException("No kafka producer available to use"));
        return new RollingKafkaDestination(
                key,
                getFrequency(),
                getSchedule(),
                getRollSize(),
                System.currentTimeMillis(),
                stroomKafkaProducer,
                recordKey,
                topic,
                flushOnSend);
    }

    @PipelineProperty(description = "The Kafka config to use.", displayPriority = 1)
    @PipelinePropertyDocRef(types = KafkaConfigDoc.DOCUMENT_TYPE)
    public void setKafkaConfig(final DocRef kafkaConfigRef) {
        this.kafkaConfigRef = kafkaConfigRef;
    }

    @PipelineProperty(
            description = "The topic to send the record to. Replacement variables can be used in path strings such as ${feed}.",
            displayPriority = 2)
    public void setTopic(final String topic) {
        this.topic = pathCreator.replaceAll(topic);
    }

    @PipelineProperty(
            description = "The record key to apply to records, used to select partition. Replacement variables can be used in path strings such as ${feed}.",
            displayPriority = 3)
    public void setRecordKey(final String recordKey) {
        this.recordKey = pathCreator.replaceAll(recordKey);
    }

    @PipelineProperty(description = "Choose how frequently files are rolled.",
            defaultValue = "1h",
            displayPriority = 4)
    public void setFrequency(final String frequency) {
        super.setFrequency(frequency);
    }

    @PipelineProperty(description = "Provide a cron expression to determine when files are rolled.",
            displayPriority = 5)
    public void setSchedule(final String expression) {
        super.setSchedule(expression);
    }

    @PipelineProperty(
            description = "Not available in this version.",
            defaultValue = "false",
            displayPriority = 6)
    public void setFlushOnSend(final boolean flushOnSend) {
        this.flushOnSend = flushOnSend;
    }

    @PipelineProperty(description = "Choose the maximum size that a stream can be before it is rolled.",
            defaultValue = "100M",
            displayPriority = 7)
    public void setRollSize(final String rollSize) {
        super.setRollSize(rollSize);
    }
}
