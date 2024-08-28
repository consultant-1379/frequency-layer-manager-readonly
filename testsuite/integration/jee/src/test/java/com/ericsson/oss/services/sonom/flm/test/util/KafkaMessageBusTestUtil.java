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
package com.ericsson.oss.services.sonom.flm.test.util;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.consumer.single.AutomaticCommitKafkaConsumer;
import com.ericsson.oss.services.sonom.common.kafka.deserializer.KafkaJsonDeserializer;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * Class containing util methods to work with Kafka message bus
 */
public final class KafkaMessageBusTestUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageBusTestUtil.class);
    private static final String BOOTSTRAP_SERVER = getEnvironmentValue("MESSAGING_HOST_URL");
    private static final String POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME = getEnvironmentValue("POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME");
    private static final String POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME = getEnvironmentValue("POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME");
    private static final String PA_POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME = getEnvironmentValue("PA_POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME",
            "flmPaPolicyOutputTopic");
    private static final String POLICY_INPUT_EVENT_GROUP_ID = "flmPolicyInputTestGroupId";
    private static final String POLICY_OUTPUT_EVENT_GROUP_ID = "flmPolicyOutputTestGroupId";
    private static final String PA_POLICY_OUTPUT_EVENT_GROUP_ID = "flmPaPolicyOutputTestGroupId";

    private static final Map<String, List<PolicyInputEvent>> CONSUMED_POLICY_INPUT_EVENTS = new HashMap<>();
    private static final Map<String, List<PolicyOutputEvent>> CONSUMED_POLICY_OUTPUT_EVENTS = new HashMap<>();
    private static final Map<String, List<PaPolicyOutputEvent>> CONSUMED_PA_POLICY_OUTPUT_EVENTS = new HashMap<>();

    private static AutomaticCommitKafkaConsumer<String, PolicyInputEvent> inputConsumer;
    private static AutomaticCommitKafkaConsumer<String, PolicyOutputEvent> outputConsumer;
    private static AutomaticCommitKafkaConsumer<String, PaPolicyOutputEvent> paOutputConsumer;
    private static KafkaMessageProducer<String, String> kafkaMessageProducer;

    private KafkaMessageBusTestUtil() {
    }

    /**
     * Sends {@link PolicyInputEvent} to Kafka topic.
     *
     * @param policyInputEvent
     *            the {@link PolicyInputEvent} to send.
     * @return a {@link Future} to verify the message has been put onto topic.
     */
    public static Future<RecordMetadata> sendPolicyInputToPolicyInputTopic(final PolicyInputEvent policyInputEvent) {
        if (kafkaMessageProducer == null) {
            kafkaMessageProducer = new KafkaMessageProducer<>(
                    BOOTSTRAP_SERVER, StringSerializer.class.getName(), StringSerializer.class.getName(), new Properties());
        }
        final Callback producerCallback = (metadata, e) -> {
            if (e == null) {
                LOGGER.info("Test Kafka records successfully sent to topic {} for execution id {}", metadata.topic(),
                        policyInputEvent.getExecutionId());
            } else {
                LOGGER.error("Exception while sending test Kafka record for execution id {}. Exception: {}.", policyInputEvent.getExecutionId(), e);
            }
        };
        return kafkaMessageProducer.sendKafkaMessage(POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME,
                policyInputEvent.getExecutionId(), policyInputEvent.toJson(), producerCallback);
    }

    /**
     * Gets the Kafka consumer for the PolicyInputEvent Kafka topic.
     *
     * @return a {@link AutomaticCommitKafkaConsumer} to be shutdown later.
     * @throws KafkaConsumerInstantiationException
     *             to fail test if it fails to instantiate.
     */
    public static AutomaticCommitKafkaConsumer<String, PolicyInputEvent> getPolicyInputTopicConsumer()
            throws KafkaConsumerInstantiationException {
        if (inputConsumer == null) {
            inputConsumer = new AutomaticCommitTestKafkaConsumer<>(
                    new KafkaJsonDeserializer<>(PolicyInputEvent.class), POLICY_INPUT_EVENT_GROUP_ID,
                    Collections.singletonList(POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME), CONSUMED_POLICY_INPUT_EVENTS);
            inputConsumer.consumeRecords();
        }
        return inputConsumer;
    }

    /**
     * Gets the Kafka consumer for the PolicyOutputEvent Kafka topic.
     *
     * @return a {@link AutomaticCommitKafkaConsumer} to be shutdown later.
     * @throws KafkaConsumerInstantiationException
     *             to fail test if it fails to instantiate.
     */
    public static AutomaticCommitKafkaConsumer<String, PolicyOutputEvent> getPolicyOutputTopicConsumer()
            throws KafkaConsumerInstantiationException {
        if (outputConsumer == null) {
            outputConsumer = new AutomaticCommitTestKafkaConsumer<>(
                    new KafkaJsonDeserializer<>(PolicyOutputEvent.class), POLICY_OUTPUT_EVENT_GROUP_ID,
                    Collections.singletonList(POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME), CONSUMED_POLICY_OUTPUT_EVENTS);
            outputConsumer.consumeRecords();
        }
        return outputConsumer;
    }

    /**
     * Gets the Kafka consumer for the PaPolicyOutputEvent Kafka topic.
     *
     * @return a {@link AutomaticCommitKafkaConsumer} to be shutdown later.
     * @throws KafkaConsumerInstantiationException
     *             to fail test if it fails to instantiate.
     */
    public static AutomaticCommitKafkaConsumer<String, PaPolicyOutputEvent> getPaPolicyOutputTopicConsumer()
            throws KafkaConsumerInstantiationException {
        if (paOutputConsumer == null) {
            paOutputConsumer = new AutomaticCommitTestKafkaConsumer<>(
                    new KafkaJsonDeserializer<>(PaPolicyOutputEvent.class), PA_POLICY_OUTPUT_EVENT_GROUP_ID,
                    Collections.singletonList(PA_POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME), CONSUMED_PA_POLICY_OUTPUT_EVENTS);
            paOutputConsumer.consumeRecords();
        }
        return paOutputConsumer;
    }

    /**
     * Get the latest {@link List} of {@link PolicyInputEvent}s consumed by their executionId.
     *
     * @param executionId
     *            of desired PolicyInputEvents.
     * @return Unmodifiable list of PolicyInputEvent.
     */
    public static List<PolicyInputEvent> getLatestPolicyInputFromConsumerByExecutionId(final String executionId) {
        LOGGER.info("Getting latest policy input from consumer {} on PolicyInputTopic", executionId);
        return hasConsumedFromPolicyInputTopic(executionId)
                ? Collections.unmodifiableList(CONSUMED_POLICY_INPUT_EVENTS.remove(executionId))
                : Collections.emptyList();
    }

    /**
     * Get the latest {@link List} of {@link PolicyOutputEvent}s consumed by their executionId.
     *
     * @param executionId
     *            of desired PolicyOutputEvents.
     * @return Unmodifiable list of PolicyOutputEvent.
     */
    public static List<PolicyOutputEvent> getLatestPolicyOutputFromConsumerByExecutionId(final String executionId) {
        LOGGER.info("Getting latest policy output from consumer {} on PolicyOutputTopic", executionId);
        return hasConsumedFromPolicyOutputTopic(executionId)
                ? Collections.unmodifiableList(CONSUMED_POLICY_OUTPUT_EVENTS.remove(executionId))
                : Collections.emptyList();
    }

    public static List<PaPolicyOutputEvent> getLatestPaPolicyOutputFromConsumerByPaExecutionId(final String paExecutionId) {
        LOGGER.info("Getting latest pa policy output from consumer {} on PaPolicyOutputTopic", paExecutionId);
        return hasConsumedFromPaPolicyOutputTopic(paExecutionId)
                ? Collections.unmodifiableList(CONSUMED_PA_POLICY_OUTPUT_EVENTS.remove(paExecutionId))
                : Collections.emptyList();
    }

    /**
     * Verify that consumer has consumed from Policy Input Topic
     *
     * @param executionId
     *            of desired PolicyOutputEvents.
     * @return if the consumer has consumed with desired executionId
     */
    public static boolean hasConsumedFromPolicyInputTopic(final String executionId) {
        LOGGER.info("Checking if consumed executionId {} in items retrieved from '{}'", executionId, POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME);
        return CONSUMED_POLICY_INPUT_EVENTS.containsKey(executionId);
    }

    /**
     * Verify that consumer has consumed from Policy Output Topic
     *
     * @param executionId
     *            of desired PolicyOutputEvents.
     * @return if the consumer has consumed with desired executionId
     */
    public static boolean hasConsumedFromPolicyOutputTopic(final String executionId) {
        LOGGER.info("Checking if consumed executionId {} in items retrieved from '{}'", executionId, POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME);
        return CONSUMED_POLICY_OUTPUT_EVENTS.containsKey(executionId);
    }

    public static boolean hasConsumedFromPaPolicyOutputTopic(final String paExecutionId) {
        LOGGER.info("Checking if consumed paExecutionId {} in items retrieved from '{}'", paExecutionId, PA_POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME);
        return CONSUMED_PA_POLICY_OUTPUT_EVENTS.containsKey(paExecutionId);
    }

    private static class AutomaticCommitTestKafkaConsumer<T> extends AutomaticCommitKafkaConsumer<String, T> {
        private final Map<String, List<T>> consumedMap;

        private AutomaticCommitTestKafkaConsumer(final Deserializer<T> valueDeserializer,
                final String groupId,
                final List<String> topics,
                final Map<String, List<T>> consumedMap) throws KafkaConsumerInstantiationException {
            super(BOOTSTRAP_SERVER, new StringDeserializer(), valueDeserializer, groupId, topics);
            this.consumedMap = consumedMap;
        }

        @Override
        protected KafkaRecordHandler<ConsumerRecord<String, T>> getKafkaRecordHandler() {
            return record -> {
                String key = record.key();
                if (record.value() instanceof PolicyOutputEvent) {
                    final PolicyOutputEvent policyOutputEvent = (PolicyOutputEvent) record.value();
                    key = policyOutputEvent.getExecutionId();
                } else if (record.value() instanceof PaPolicyOutputEvent) {
                    final PaPolicyOutputEvent paPolicyOutputEvent = (PaPolicyOutputEvent) record.value();
                    key = paPolicyOutputEvent.getPaExecutionId();
                }

                LOGGER.info("Record consumed {} : {}", key, record.value());
                if (!consumedMap.containsKey(key)) {
                    consumedMap.put(key, new ArrayList<>());
                }
                consumedMap.get(key).add(record.value());
            };
        }
    }
}
