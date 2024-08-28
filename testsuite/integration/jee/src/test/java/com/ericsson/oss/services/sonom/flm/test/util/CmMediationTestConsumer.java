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

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.api.Model;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.consumer.single.AutomaticCommitKafkaConsumer;
import com.ericsson.oss.services.sonom.common.kafka.deserializer.KafkaJsonDeserializer;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.common.test.util.IntegrationTestUtils;

/**
 * This consumer subscribes to the Kafka topic CM mediation topic and reads the last message on the topic or waits until a message is received. <br>
 * <p>
 * The Kafka messaging host url (ie hostname:port) and CM mediation topic name must be defined in System environment variables:
 * <ul>
 * <li><code>MESSAGING_HOST_URL</code></li>
 * <li><code>CM_MEDIATION_TOPIC</code></li>
 * </ul>
 * <p>
 * Note: The Kafka "group.id" (<code>GROUP_ID</code>) used is unique each time a new instance is created to allow us to read all the message on the
 * topic.
 */
public class CmMediationTestConsumer extends AutomaticCommitKafkaConsumer<String, Model> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmMediationTestConsumer.class);
    private static final String MESSAGING_HOST_URL = getEnvironmentValue("MESSAGING_HOST_URL");
    private static final String CM_MEDIATION_TOPIC = getEnvironmentValue("CM_MEDIATION_TOPIC");
    private static final String AUTO_OFFSET_RESET_CONFIG_VALUE = "earliest";
    private static final String KAFKA_CONSUMER_ID = "cmMediationTestConsumer";
    private static final String GROUP_ID = "flmTestGroupId:" + UUID.randomUUID().toString();
    private static final long POLLING_TIME_MS = TimeUnit.SECONDS.toMillis(1);

    private Model messagesReceived;

    private static final KafkaJsonDeserializer<String> KEY_DESERIALIZER = new KafkaJsonDeserializer<>(String.class);
    private static final KafkaJsonDeserializer<Model> VALUE_DESERIALIZER = new KafkaJsonDeserializer<>(Model.class);

    public CmMediationTestConsumer() throws KafkaConsumerInstantiationException {
        super(MESSAGING_HOST_URL, KEY_DESERIALIZER, VALUE_DESERIALIZER, GROUP_ID, Collections.singletonList(CM_MEDIATION_TOPIC),
                POLLING_TIME_MS, addAdditionalProperties());
        consumeRecords();
        // give time for the Kafka consumer to start
        IntegrationTestUtils.sleep(10, TimeUnit.SECONDS);
    }

    private static Properties addAdditionalProperties() {
        final Properties additionalProperties = new Properties();
        additionalProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG_VALUE);
        additionalProperties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, KAFKA_CONSUMER_ID);
        return additionalProperties;
    }

    @Override
    protected KafkaRecordHandler<ConsumerRecord<String, Model>> getKafkaRecordHandler() {
        return record -> {
            LOGGER.info("Handling received record: {}", record);
            messagesReceived = record.value();
        };
    }

    /**
     * Wait until a message is found on the topic. It checks for a message every 5 seconds
     */
    public void waitUntilMessagesReceived() {
        while (getLastMessage() == null) {
            LOGGER.info("Sleeping for {} seconds...", 5);
            IntegrationTestUtils.sleep(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Get the last message received on the topic
     * 
     * @return the last message containing the {@link Model}
     */
    public Model getLastMessage() {
        return messagesReceived;
    }

    @Override
    public void close() {
        LOGGER.info("Closing test consumer");
        super.shutdown();
    }
}
