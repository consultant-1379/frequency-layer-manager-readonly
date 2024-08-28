/*
 * -----------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.SEND_ACTIVATION_POLICY_TO_TOPIC_ERROR;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.activation.kafka.ActivationMode;
import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicy;
import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicyKey;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.common.kafka.serializer.KafkaJsonSerializer;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;

/**
 * ActivationPolicySenderImpl is used to send {@link ActivationPolicy} object to the ChangeMediation Kafka Topic
 * based on the {@link com.ericsson.oss.services.sonom.flm.service.api.executions.Execution}'s id and the source of change.
 */
public class ActivationPolicySenderImpl implements ActivationPolicySender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivationPolicySenderImpl.class);

    private static final String ACKS_ALL = "all";
    private static final String IDEMPOTENCE_CONFIG_ENABLED = "true";
    //Retry every 1.5 seconds, 400 times -> 10 minutes
    private static final String ACTIVATION_POLICY_MESSAGING_PRODUCER_RETRY_BACKOFF_MS = "1500";
    private static final String ACTIVATION_POLICY_MESSAGING_PRODUCER_RETRIES = "400";

    private static final String KAFKA_BOOTSTRAP_SERVER = getEnvironmentValue("BOOTSTRAP_SERVER");

    private static final String FILTER_EXECUTION_ID = "executionId";
    private static final String EMPTY_CRON_VALUE = "";
    private static final String FILTER_CHANGE_TYPE = "changeType";

    private final String cmChangeMediationKafkaTopicName = getEnvironmentValue("CHANGE_MEDIATION_TOPIC");

    private final KafkaMessageProducer<ActivationPolicyKey, ActivationPolicy> kafkaMessageProducer;

    public ActivationPolicySenderImpl() {
        final Properties producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.ACKS_CONFIG, ACKS_ALL);
        producerProperties.setProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, ACTIVATION_POLICY_MESSAGING_PRODUCER_RETRY_BACKOFF_MS);
        producerProperties.setProperty(ProducerConfig.RETRIES_CONFIG, ACTIVATION_POLICY_MESSAGING_PRODUCER_RETRIES);
        producerProperties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, IDEMPOTENCE_CONFIG_ENABLED);

        final Optional<String> kafkaBootstrapServer = Optional.ofNullable(KAFKA_BOOTSTRAP_SERVER);

        kafkaMessageProducer = kafkaBootstrapServer.map(server -> new KafkaMessageProducer<ActivationPolicyKey, ActivationPolicy>(
                server,
                KafkaJsonSerializer.class.getName(),
                KafkaJsonSerializer.class.getName(),
                producerProperties)).orElse(null);
    }

    /**
     * Will be used for testing purposes only.
     * @param kafkaMessageProducer an instance of {@link ActivationPolicySender}
     */
    public ActivationPolicySenderImpl(final KafkaMessageProducer<ActivationPolicyKey, ActivationPolicy> kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @Override
    public void sendActivationPolicyToKafka(final String executionId, final String sourceOfChange) throws FlmAlgorithmException {
        final ActivationPolicyKey activationPolicyKey = new ActivationPolicyKey(sourceOfChange);

        final Map<String, Object> filterCriteria = new HashMap<>(1);
        filterCriteria.put(FILTER_EXECUTION_ID, executionId);
        filterCriteria.put(FILTER_CHANGE_TYPE, ChangeElement.ChangeType.OPTIMIZATION.toString());

        final ActivationPolicy activationPolicy = new ActivationPolicy(
            sourceOfChange,
            ActivationMode.IMMEDIATE,
            EMPTY_CRON_VALUE,
            filterCriteria);

        try {
            final KafkaCallback callback = new KafkaCallback();
            kafkaMessageProducer.sendKafkaMessage(cmChangeMediationKafkaTopicName, activationPolicyKey, activationPolicy, callback);
        } catch (final Exception e) {
            LOGGER.warn("Exception while sending the Activation Policy message to Kafka: {}", e.getMessage());
            throw new FlmAlgorithmException(SEND_ACTIVATION_POLICY_TO_TOPIC_ERROR, e);
        } finally {
            kafkaMessageProducer.close();
        }
    }

    private static class KafkaCallback implements Callback {
        @Override
        public void onCompletion(final RecordMetadata metadata, final Exception e) {
            if (e == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Kafka records successfully sent to topic {}", metadata.topic());
                }
            } else {
                LOGGER.warn("Exception while sending the Activation Policy message to Kafka: {}", e.getMessage());
            }
        }
    }
}
