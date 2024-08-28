/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.CommitType;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;

/**
 * This class is handling a batch of kafka messages that contains generic policy output events by implementing {@link KafkaRecordHandler}. It uses a
 * {@link KafkaConsumerWrapper} that is used to be able to test the system. It also uses a {@link KafkaRecordHandler} object to pass the Kafka
 * messages to the client for processing. Some Kafka messages will be dropped based on the executionNeeded and counters objects. The class also
 * calculates the commits that should be sent to Kafka.
 *
 * @param <T>
 *            - Type of message to consume.
 */

public abstract class GenericExecutionConsumer<T> implements KafkaRecordHandler<ConsumerRecords<String, T>> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(GenericExecutionConsumer.class);
    private final int consumerId;
    private final ConcurrentMap<String, ExecutionCounter> counters;
    private final KafkaRecordHandler<T> kafkaRecordHandler;
    private KafkaConsumerWrapper<T> kafkaConsumerWrapper;
    private final Map<Integer, ConsumerRecord<String, T>> partitionToOffsetMapping = new HashMap<>();

    protected GenericExecutionConsumer(final int consumerId, final ConcurrentMap<String, ExecutionCounter> counters,
            final KafkaRecordHandler<T> kafkaRecordHandler) throws BadSetupException {
        this.consumerId = consumerId;
        this.counters = counters;
        this.kafkaRecordHandler = kafkaRecordHandler;
        validateKafkRecordHandler();
        validateCounters();
    }

    public void setKafkaConsumerWrapper(final KafkaConsumerWrapper<T> kafkaConsumerWrapper) throws BadSetupException {
        this.kafkaConsumerWrapper = kafkaConsumerWrapper;
        validateConsumerWrapper();
    }

    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void handle(final ConsumerRecords<String, T> batch) {
        LOGGER.debug("Number of {} ConsumerRecords will be handled by consumer {}", batch.count(), consumerId);
        if (batch.count() == 0) {
            LOGGER.debug("No consumer records. Doing nothing at consumer {}", consumerId);
            return;
        } else {
            LOGGER.info("Number of {} records received in batch from Kafka", batch.count());
        }

        //we process the records by partitions
        StreamSupport.stream(batch.spliterator(), false).collect(Collectors.groupingBy(ConsumerRecord::partition))
                .forEach((partition, records) -> records.forEach(record -> {
                    handleRecord(record);
                    addOffset(record);
                }));
        LOGGER.info("Committing at consumer {}", consumerId);
        kafkaConsumerWrapper.commitRecords(CommitType.SYNCHRONOUS, partitionToOffsetMapping);
        LOGGER.info("Committed at consumer {}", consumerId);
    }

    /**
     * Method to consume records by calling {@link KafkaConsumerWrapper}.
     *
     * @throws BadSetupException
     *             - when objects in the messagehandler aren't setup properly
     **/

    public void consumeRecords() throws BadSetupException {
        LOGGER.info("Starting to consume records in consumer {}", consumerId);
        validateConsumerWrapper();
        kafkaConsumerWrapper.consumeRecords();
    }

    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void shutDown() {
        kafkaConsumerWrapper.shutdown();
    }

    protected abstract void handleRecord(ConsumerRecord<String, T> record);

    private void addOffset(final ConsumerRecord<String, T> record) {
        partitionToOffsetMapping.put(record.partition(), record);
    }

    private void validateConsumerWrapper() throws BadSetupException {
        if (kafkaConsumerWrapper == null) {
            throw new BadSetupException();
        }
    }

    private void validateKafkRecordHandler() throws BadSetupException {
        if (kafkaRecordHandler == null) {
            throw new BadSetupException();
        }
    }

    private void validateCounters() throws BadSetupException {
        if (counters == null) {
            throw new BadSetupException();
        }
    }

    public int getConsumerId() {
        return consumerId;
    }

    public ConcurrentMap<String, ExecutionCounter> getCounters() {
        return counters;
    }

    public KafkaRecordHandler<T> getKafkaRecordHandler() {
        return kafkaRecordHandler;
    }

    public KafkaConsumerWrapper getKafkaConsumerWrapper() {
        return kafkaConsumerWrapper;
    }

}