/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler.utils;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.errors.InterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.common.kafka.serializer.KafkaJsonSerializer;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class is producing PolicyOutputEvent messages to read Kafka in batches. It is used in manual tests.
 */
public class TestPolicyOutputEventProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPolicyOutputEventProducer.class);
    private static final Class<KafkaJsonSerializer> KEY_SERIALIZER = KafkaJsonSerializer.class;
    private static final Class<KafkaJsonSerializer> VALUE_SERIALIZER = KafkaJsonSerializer.class;
    private static final String TOPIC = "flmPolicyOutputEventTopic";
    private static final KafkaMessageProducer<String, PolicyOutputEvent> producer = new KafkaMessageProducer<>(
            "eric-data-message-bus-kf-0.eric-data-message-bus-kf.flm-test-kafka-consumers:9092",
            KEY_SERIALIZER.getName(), VALUE_SERIALIZER.getName());

    private TestPolicyOutputEventProducer() {
    }

    public static ExecutorService produceKafkaRecords(final Integer initialSectorId, final List<String> executionIds, final int batchSize,
            final int batchesToSend, final int waitBetweenBatches) {
        final Callback callback = (recordMetadata, e) -> {
            assertNull("Failed to send Kafka message", e);
            assertNotNull(recordMetadata);
        };
        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                int sectorId = initialSectorId;
                for (int b = 0; b < batchesToSend; b++) {
                    LOGGER.info("Sending batch {}", b);
                    for (int offset = 0; offset < batchSize; offset++) {
                        for (final String executionId : executionIds) {
                            producer.sendKafkaMessage(TOPIC, String.valueOf(executionId),
                                    TestDataBuilder.buildPolicyOutputEvent(executionId, new Long(sectorId + offset),
                                            TestDataBuilder.buildLoadBalancingQuanta()),
                                    callback);
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(waitBetweenBatches);
                    sectorId += batchSize;
                }
                producer.close();
            } catch (final InterruptedException e) {
                throw new InterruptException(e);
            }
        });
        return service;
    }
}
