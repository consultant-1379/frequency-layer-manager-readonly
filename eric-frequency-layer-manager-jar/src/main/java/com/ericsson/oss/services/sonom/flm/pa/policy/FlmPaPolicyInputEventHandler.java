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

package com.ericsson.oss.services.sonom.flm.pa.policy;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.flm.FlmPolicyInputEventHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * This class sends policy Input Event on to the kafka topic.
 */
public class FlmPaPolicyInputEventHandler {
    private static final long FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlmPaPolicyInputEventHandler.class);
    private static final String POLICY_INPUT_MESSAGING_PRODUCER_RETRY_BACKOFF_MS = "1500";
    private static final String POLICY_INPUT_MESSAGING_PRODUCER_RETRIES = "400";
    private static final String POLICY_INPUT_MESSAGING_PRODUCER_DELIVERY_TIMEOUT_MS = getEnvironmentValue("KAFKA_PRODUCER_DELIVERY_TIMEOUT_MS",
            "240000");
    private static final String TOPIC_NAME = getEnvironmentValue("PA_POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME", "flmPaPolicyInputTopic");

    static {
        final TimeUnit flmPaSentMessagesCounterTimeoutUnit = TimeUnit
                .valueOf(getEnvironmentValue("FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT_UNIT", "SECONDS"));
        final long flmPaSentMessagesCounterTimeoutValue = Long.parseLong(getEnvironmentValue("FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT_MS_VALUE", "60"));
        FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT = flmPaSentMessagesCounterTimeoutUnit.toNanos(flmPaSentMessagesCounterTimeoutValue);
    }

    private final String kafkaBootstrapServer = getEnvironmentValue("BOOTSTRAP_SERVER");
    private final PAExecutionDao executionDao;

    public FlmPaPolicyInputEventHandler(final PAExecutionDao executionDao) {
        this.executionDao = executionDao;
    }

    static List<ProducerRecord> getRecordsToSend(final List<String> paPolicyInputMessagesList) {
        return paPolicyInputMessagesList.stream()
                .map(policyInputEvent -> new ProducerRecord<>(FlmPaPolicyInputEventHandler.TOPIC_NAME, policyInputEvent))
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
     * @param paPolicyInputEventList
     *            list of policyInputEvent strings to be sent to the topic
     * @param execution
     *            the execution
     * @throws PAExecutionException
     *             thrown if there is any error sending to the kafka topic
     */
    public void sendToKafkaTopic(final List<String> paPolicyInputEventList, final PAExecution execution) throws PAExecutionException {

        LOGGER.info("Sending {} records to topic: {} with PAExecution ID: {}",
                paPolicyInputEventList.size(), TOPIC_NAME, execution.getId());

        try (final KafkaMessageProducer<String, String> kafkaMessageProducer = getKafkaMessageProducer(kafkaBootstrapServer)) {
            final List<ProducerRecord> records = getRecordsToSend(paPolicyInputEventList);

            final CountDownLatch latch = getLatch(records);
            final AtomicInteger counter = new AtomicInteger(0);
            final FlmPolicyInputEventHandler.KafkaCallback callback = new FlmPolicyInputEventHandler.KafkaCallback(latch, counter);
            execution.setNumPaPolicyInputEventsSent(records.size());
            updatePAExecution(execution);
            kafkaMessageProducer.sendKafkaMessagesBatch(records, callback);

            final boolean didLatchTimeout = !latch.await(FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT, TimeUnit.NANOSECONDS);
            if (didLatchTimeout) {
                LOGGER.warn("Count down latch for {} records timed out after {} {}. Total of {} records sent with PAExecution ID: {}", records.size(),
                        FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT, TimeUnit.NANOSECONDS, counter.get(), execution.getId());
            } else {
                LOGGER.info("Callback received with {} records before timed out of {} {}. Total of {} records sent with PAExecution ID: {}",
                        records.size(), FLM_PA_SENT_MESSAGES_COUNTER_TIMEOUT, TimeUnit.NANOSECONDS, counter.get(), execution.getId());
            }
        } catch (final KafkaException | InterruptedException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error when sending records to topic: {} with PAExecution ID: {}", TOPIC_NAME, execution.getId(), e);
            throw new PAExecutionException(e);
        }
    }

    CountDownLatch getLatch(final List<ProducerRecord> records) {
        return new CountDownLatch(records.size());
    }

    KafkaMessageProducer<String, String> getKafkaMessageProducer(final String kafkaBootstrapServer) {
        return new KafkaMessageProducer<>(kafkaBootstrapServer, StringSerializer.class.getName(), StringSerializer.class.getName(),
                addAdditionalProperties());
    }

    protected void updatePAExecution(final PAExecution execution) {
        try {
            final int updatedRows = executionDao.update(execution);
            if (updatedRows == 0) {
                LOGGER.warn("Failed to persist update for PAExecution: {}", execution);
            }
        } catch (final SQLException e) {
            LOGGER.warn("Failed to persist PAExecution : {}", e.getMessage());
        }
    }
}
