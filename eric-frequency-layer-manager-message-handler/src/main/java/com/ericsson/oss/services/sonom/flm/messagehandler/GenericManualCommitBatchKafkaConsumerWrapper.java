/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020-2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.CommitType;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.consumer.batch.ManualCommitBatchKafkaConsumer;
import com.ericsson.oss.services.sonom.common.kafka.deserializer.KafkaJsonDeserializer;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * This class extends the {@link ManualCommitBatchKafkaConsumer} to handle Kafka messages. It does not directly handle the messages but calls the
 * kafkaRecordHandler that is implemented by {@link GenericExecutionConsumer}.
 * @param <T>
 *            -type of record to consume.
 */
public abstract class GenericManualCommitBatchKafkaConsumerWrapper<T> extends ManualCommitBatchKafkaConsumer<String, T>
        implements KafkaConsumerWrapper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericManualCommitBatchKafkaConsumerWrapper.class);
    private static final KafkaJsonDeserializer<String> KEY_DESERIALIZER = new KafkaJsonDeserializer<>(String.class);
    private static final String KAFKA_BOOTSTRAP_SERVER = getEnvironmentValue("BOOTSTRAP_SERVER");
    private static final String AUTO_OFFSET_RESET_CONFIG_VALUE = "earliest";
    private static final String MAX_POLL_INTERVAL_MS_CONFIG = "600000";
    private static final String MAX_POLL_RECORDS = "120";
    private static final int KAFKA_MAX_ATTEMPTS = 30;
    private static final long KAFKA_INTERVAL_MILLIS = 10000L;

    private KafkaRecordHandler<ConsumerRecords<String, T>> recordHandler;

    public GenericManualCommitBatchKafkaConsumerWrapper(final String kafkaGroupId,
            final String kafkaTopic,
            final String clientIdConfig,
            final int consumerId,
            final KafkaJsonDeserializer<T> kafkaJsonDeserializer)
            throws KafkaConsumerInstantiationException {
        super(KAFKA_BOOTSTRAP_SERVER, KEY_DESERIALIZER,
                kafkaJsonDeserializer,
                kafkaGroupId,
                Collections.singletonList(kafkaTopic),
                DEFAULT_POLL_TIMEOUT_IN_MS,
                addAdditionalProperties(clientIdConfig, consumerId),
                getMoreRetry());
    }

    private static Retry getMoreRetry() {
        final RetryConfig config = RetryConfig.custom()
                .maxAttempts(KAFKA_MAX_ATTEMPTS)
                .intervalFunction(IntervalFunction.of(KAFKA_INTERVAL_MILLIS))
                .retryExceptions(Throwable.class)
                .build();
        return Retry.of("KafkaConsumerRetry", config);
    }

    /**
     * This method is called to set the recordHandler that will handle Kafka messages.
     * @param recordHandler
     *            a Kafka record handler implemented by ExecutionConsumer
     * @throws BadSetupException
     *             when recordHandler is null
     */
    @Override
    public void setKafkaRecordHandler(final KafkaRecordHandler<ConsumerRecords<String, T>> recordHandler) throws BadSetupException {
        validateKafkaRecordHandler(recordHandler);
        this.recordHandler = recordHandler;
    }

    /**
     * The method is called by ManualCommitBatchKafkaConsumer at construction time
     * @return a Kafka message handler implemented by ExecutionConsumer
     */
    @Override
    protected KafkaRecordHandler<ConsumerRecords<String, T>> getKafkaRecordHandler() {
        return consumerRecords -> recordHandler.handle(consumerRecords);
    }

    /**
     * The method just delegates the call to ManualCommitBatchKafkaConsumer.
     * @param commitType
     *            the type of commit
     * @param partitionOffsetMapping
     *            records containing offsets grouped by partitions
     */
    @Override
    public void commitRecords(final CommitType commitType, final Map<Integer, ConsumerRecord<String, T>> partitionOffsetMapping) {
        try {
            super.commit(commitType, partitionOffsetMapping);
        } catch (final Exception e) {
            LOGGER.warn("Failed to commit batch. Kafka will send these messages again. Reason: {}", e.getMessage());
        }
    }

    private static Properties addAdditionalProperties(final String clientIdConfig, final int consumerId) {
        final Properties additionalProperties = new Properties();
        additionalProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG_VALUE);
        additionalProperties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS_CONFIG);
        additionalProperties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        //To have unique Kafka Consumer
        additionalProperties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientIdConfig + "-" + consumerId);
        return additionalProperties;
    }

    private void validateKafkaRecordHandler(final KafkaRecordHandler<ConsumerRecords<String, T>> recordHandler)
            throws BadSetupException {
        if (recordHandler == null) {
            throw new BadSetupException();
        }
    }

}