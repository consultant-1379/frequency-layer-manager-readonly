/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.OPTIMIZATION_ELEMENT_SENT_PERSISTENCE_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.SEND_RECORD_TO_TOPIC_ERROR;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class sends policy Input Event on to the kafka topic.
 */
public class FlmPolicyInputEventHandler {
    private static final long FLM_SENT_MESSAGES_COUNTER_TIMEOUT;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlmPolicyInputEventHandler.class);
    private static final String POLICY_INPUT_MESSAGING_PRODUCER_RETRY_BACKOFF_MS = "1500";
    private static final String POLICY_INPUT_MESSAGING_PRODUCER_RETRIES = "400";
    private static final String POLICY_INPUT_MESSAGING_PRODUCER_DELIVERY_TIMEOUT_MS = getEnvironmentValue("KAFKA_PRODUCER_DELIVERY_TIMEOUT_MS",
            "240000");

    private static final String TOPIC_NAME = "flmPolicyInputTopic";
    private final String kafkaBootstrapServer = getEnvironmentValue("BOOTSTRAP_SERVER");

    static {
        final TimeUnit flmSentMessagesCounterTimeoutUnit = TimeUnit.valueOf(getEnvironmentValue("FLM_SENT_MESSAGES_COUNTER_TIMEOUT_UNIT", "SECONDS"));
        final long flmSentMessagesCounterTimeoutValue = Long.parseLong(getEnvironmentValue("FLM_SENT_MESSAGES_COUNTER_TIMEOUT_MS_VALUE", "60"));
        FLM_SENT_MESSAGES_COUNTER_TIMEOUT = flmSentMessagesCounterTimeoutUnit.toNanos(flmSentMessagesCounterTimeoutValue);
    }

    private final ExecutionDao executionDao;

    public FlmPolicyInputEventHandler(final ExecutionDao executionDao) {
        this.executionDao = executionDao;
    }

    static List<ProducerRecord> getRecordsToSend(final List<String> policyInputMessagesList, final String topicName) {
        return policyInputMessagesList.stream().map(policyInputEvent -> new ProducerRecord<>(topicName, policyInputEvent))
                .collect(Collectors.toList());
    }

    private static Properties addAdditionalProperties() {
        final Properties additionalProperties = new Properties();
        additionalProperties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        additionalProperties.setProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, POLICY_INPUT_MESSAGING_PRODUCER_RETRY_BACKOFF_MS);
        additionalProperties.setProperty(ProducerConfig.RETRIES_CONFIG, POLICY_INPUT_MESSAGING_PRODUCER_RETRIES);
        additionalProperties.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, POLICY_INPUT_MESSAGING_PRODUCER_DELIVERY_TIMEOUT_MS);
        return additionalProperties;
    }

    /**
     * Send records to the Kafka topic.
     *
     * @param policyInputEventList
     *            list of policyInputEvent objects to be sent to the topic
     * @param execution
     *            the execution
     * @throws FlmAlgorithmException
     *             If failure sending record to topic
     * @throws InterruptedException
     *             If interrupt occurs
     */
    public void sendToKafkaTopic(final List<String> policyInputEventList, final Execution execution)
            throws FlmAlgorithmException, InterruptedException {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), String.format("Sending %d records to topic: %s",
                policyInputEventList.size(), TOPIC_NAME)));
        }

        try (final KafkaMessageProducer<String, String> kafkaMessageProducer = getKafkaMessageProducer(kafkaBootstrapServer)) {
            final List<ProducerRecord> records = getRecordsToSend(policyInputEventList, TOPIC_NAME);

            final CountDownLatch latch = getLatch(records);
            final AtomicInteger counter = new AtomicInteger(0);
            final KafkaCallback callback = new KafkaCallback(latch, counter);
            execution.setNumOptimizationElementsSent(records.size());
            updateNumberOfOptimizationElementsSent(execution);
            kafkaMessageProducer.sendKafkaMessagesBatch(records, callback);

            final boolean didLatchTimeout = !latch.await(FLM_SENT_MESSAGES_COUNTER_TIMEOUT, TimeUnit.NANOSECONDS);
            if (didLatchTimeout) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Count down latch for %d records timed out after %d %s. Total of %d records sent",
                            records.size(), FLM_SENT_MESSAGES_COUNTER_TIMEOUT, TimeUnit.NANOSECONDS, counter.get())));
                }
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Callback received with %d records before timed out of %d %s. Total of %d records sent",
                            records.size(), FLM_SENT_MESSAGES_COUNTER_TIMEOUT, TimeUnit.NANOSECONDS, counter.get())));
                }
            }
        } catch (final KafkaException | FlmAlgorithmException e) { //NOSONAR Exception suitable logged
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Error when sending records to topic: %s", TOPIC_NAME)), e);
            }
            throw new FlmAlgorithmException(SEND_RECORD_TO_TOPIC_ERROR, e);
        }
    }

    CountDownLatch getLatch(final List<ProducerRecord> records) {
        return new CountDownLatch(records.size());
    }

    KafkaMessageProducer<String, String> getKafkaMessageProducer(final String kafkaBootstrapServer) {
        return new KafkaMessageProducer<>(kafkaBootstrapServer, StringSerializer.class.getName(), StringSerializer.class.getName(),
                addAdditionalProperties());
    }

    protected void updateNumberOfOptimizationElementsSent(final Execution execution) throws FlmAlgorithmException {
        try {
            final int updatedRows = executionDao.update(execution);
            if (updatedRows == 0) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Failed to persist update for execution: %s", execution)));
                }
                throw new FlmAlgorithmException(OPTIMIZATION_ELEMENT_SENT_PERSISTENCE_ERROR);
            }
        } catch (final SQLException | FlmAlgorithmException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Failed to persist execution : %s", e.getMessage())));
            }
            throw new FlmAlgorithmException(OPTIMIZATION_ELEMENT_SENT_PERSISTENCE_ERROR, e);
        }
    }

    /**
     * This class is used to increment the counter used for sending kafka messages.
     */
    public static class KafkaCallback implements Callback {

        private final CountDownLatch latch;
        private final AtomicInteger counter;

        /**
         * Used to keep track of the messages sent to kafka.
         *
         * @param latch
         *            A synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads
         *            complete
         * @param counter
         *            This is used to keep count of the message sent
         */
        public KafkaCallback(final CountDownLatch latch, final AtomicInteger counter) {
            this.counter = counter;
            this.latch = latch;
        }

        @Override
        public void onCompletion(final RecordMetadata metadata, final Exception exception) {
            if (exception == null) {
                latch.countDown();
                counter.incrementAndGet();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Kafka records successfully sent to topic {}", metadata.topic());
                }
            } else {
                LOGGER.warn("Exception while sending Kafka record", exception);
            }
        }
    }
}
