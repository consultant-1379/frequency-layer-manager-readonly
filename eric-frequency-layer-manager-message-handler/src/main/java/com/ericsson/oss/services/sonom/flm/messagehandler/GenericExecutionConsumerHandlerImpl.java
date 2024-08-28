/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2021
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.AbortedException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class is a Generic class for starting multiple KafkaConsumers to receive PolicyOutputEvents/PaPolicyOutputEvents. This class is extended by
 * {@link PAExecutionConsumerHandlerImpl} and {@link PoeExecutionConsumerHandlerImpl}. The client should create an instance from these classes when
 * the FLM service starts, because having multiple Consumers could involve re-balancing. For each Execution then when PolicyOutputEvents are needed
 * that consumeMessages method can be called.
 *
 * @param <T>
 *            - Type of record to be consumed
 */
public abstract class GenericExecutionConsumerHandlerImpl<T> implements ExecutionConsumerHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(GenericExecutionConsumerHandlerImpl.class);

    private static final int MIN_CONSUMER_NUM = 1;
    private static final int MAX_CONSUMER_NUM = 10;

    protected final ConcurrentMap<String, ExecutionCounter> counters = new ConcurrentHashMap<>();
    protected final List<GenericExecutionConsumer<T>> consumers = new ArrayList<>();

    /**
     * Abstract Generic class to validate List of {@link KafkaRecordHandler}s.
     * 
     * @param recordHandlers
     *            validates recordHandlers.
     * @throws BadSetupException
     *             when the number of recordHandlers is not between 1 and 10.
     */
    protected GenericExecutionConsumerHandlerImpl(final List<KafkaRecordHandler<T>> recordHandlers)
            throws BadSetupException {
        final int consumerNumber = recordHandlers.size();
        LOGGER.info("Initializing with {} recordHandlers", consumerNumber);
        validateConsumerNumber(consumerNumber);
        validateRecordHandlers(recordHandlers);
    }

    @Override
    public void consumeMessagesForExecution(final String executionId, final int messagesToConsume, final Set<Long> sectorIds) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId,
                    String.format("Starting consume messages, messagesToConsume %d for %d sectors received so far",
                            messagesToConsume, sectorIds.size())));
        }
        final ExecutionCounter counter = new ExecutionCounter(executionId, messagesToConsume, sectorIds);
        counters.put(executionId, counter);
    }

    @Override
    public void removeExecution(final String executionId) {
        counters.remove(executionId);
    }

    /**
     * Should be called when Output Events need to be received for the given Execution. It waits until all the messages have been consumed or timeout
     * reached.
     * 
     * @param executionId
     *            the unique id of an Execution
     * @param timeOutMillis
     *            the timeout valued
     * @return true if all messages have been received before the timeout has been reached, false otherwise
     * @throws InterruptedException
     *             when waiting for Kafka messages has been interrupted
     */
    @Override
    public boolean waitMessages(final String executionId, final long timeOutMillis)
            throws InterruptedException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, "Starting watch messages"));
        }
        boolean result = false;
        try {
            final ExecutionCounter counter = counters.get(executionId);
            if (counter == null) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(LoggingFormatter.formatMessage(executionId, "No counter found for execution"));
                }
                return false;
            }
            result = counters.get(executionId).await(timeOutMillis, TimeUnit.MILLISECONDS);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(executionId, String.format("Stopping to consume messages, succeeded=%s", result)));
            }
        } catch (final AbortedException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(executionId, String.format("Aborted consuming messages, reason: %s", e.getMessage())));
            }
        } finally {
            counters.remove(executionId);
        }
        return result;
    }

    protected final void processConsumers(final List<KafkaRecordHandler<T>> kafkaRecordHandlers,
            final KafkaConsumerWrapper<T> kafkaConsumerWrapper) throws KafkaConsumerInstantiationException, BadSetupException {
        final int consumerNumber = kafkaRecordHandlers.size();
        LOGGER.info("Adding {} consumers", consumerNumber);
        int consumerId = 0;

        for (final KafkaRecordHandler<T> recordHandler : kafkaRecordHandlers) {
            consumers.add(buildConsumer(kafkaConsumerWrapper, consumerId, recordHandler));
            consumerId++;
        }

        LOGGER.info("Starting to consume records with {} ExecutionConsumers", consumerNumber);

        for (final GenericExecutionConsumer<T> consumer : consumers) {
            consumer.consumeRecords();
        }
    }

    /**
     * It closes the KafkaConsumers.
     */
    @Override
    public void shutdown() {
        LOGGER.info("Shutting down all consumers");
        for (final GenericExecutionConsumer<T> consumer : consumers) {
            consumer.shutDown();
        }
        counters.forEach((executionId, counter) -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(executionId, "Shutting down counter"));
            }
            counters.get(executionId).abort();
        });
    }

    protected abstract GenericExecutionConsumer<T> buildConsumer(KafkaConsumerWrapper<T> kafkaConsumerWrapper,
            int consumerId, KafkaRecordHandler<T> recordHandler)
            throws KafkaConsumerInstantiationException, BadSetupException;

    /**
     * This method is used in tests only. It returns the {@link ExecutionCounter}s by execution ID
     * 
     * @return a map from execution ID to {@link ExecutionCounter}
     */
    Map<String, ExecutionCounter> getCounters() {
        return counters;
    }

    private void validateConsumerNumber(final int consumerNumber) throws BadSetupException {
        if (consumerNumber < MIN_CONSUMER_NUM || consumerNumber > MAX_CONSUMER_NUM) {
            throw new BadSetupException(String.format("The number of consumers %d should be between %d and %d", consumerNumber,
                    MIN_CONSUMER_NUM, MAX_CONSUMER_NUM));
        }
    }

    private void validateRecordHandlers(final List<KafkaRecordHandler<T>> recordHandlers) throws BadSetupException {
        if (recordHandlers.stream().anyMatch(Objects::isNull)) {
            throw new BadSetupException("Null recordHandler found between recordHandlers");
        }
    }

}
