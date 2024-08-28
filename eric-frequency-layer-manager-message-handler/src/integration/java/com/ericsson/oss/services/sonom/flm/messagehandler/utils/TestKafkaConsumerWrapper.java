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

package com.ericsson.oss.services.sonom.flm.messagehandler.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import com.ericsson.oss.services.sonom.common.kafka.consumer.CommitType;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.KafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * The class is used in tests to replace the ManualCommitBatchKafkaConsumerWrapper in tests. It is capable to imitating KafkaConsumer by sending Kafka
 * messages periodically It also stores the commit calls so that can be checked in tests.
 */
public class TestKafkaConsumerWrapper implements KafkaConsumerWrapper<PolicyOutputEvent> {
    private KafkaRecordHandler<ConsumerRecords<String, PolicyOutputEvent>> recordHandler;
    private final List<Map<Integer, ConsumerRecord<String, PolicyOutputEvent>>> commits = new ArrayList<>();
    private ExecutorService service;
    private final long msPollInterval;

    public TestKafkaConsumerWrapper(final long msPollInterval) {
        this.msPollInterval = msPollInterval;
    }

    @Override
    public void consumeRecords() {
        service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                final List<ConsumerRecords<String, PolicyOutputEvent>> consumerRecordsList = buildConsumerRecordsList();
                for (final ConsumerRecords<String, PolicyOutputEvent> consumerRecords : consumerRecordsList) {
                    TimeUnit.MILLISECONDS.sleep(msPollInterval);
                    recordHandler.handleRecord(consumerRecords);
                }
            } catch (final InterruptedException ignored) {
            }
        });
    }

    @Override
    public void setKafkaRecordHandler(final KafkaRecordHandler<ConsumerRecords<String, PolicyOutputEvent>> recordHandler) {
        this.recordHandler = recordHandler;
    }

    @Override
    public void commitRecords(final CommitType commitType, final Map<Integer, ConsumerRecord<String, PolicyOutputEvent>> partitionOffsetMapping) {
        commits.add(partitionOffsetMapping);
    }

    @Override
    public void shutdown() {
        service.shutdownNow();
    }

    public List<Map<Integer, ConsumerRecord<String, PolicyOutputEvent>>> getCommits() {
        return commits;
    }

    private List<ConsumerRecords<String, PolicyOutputEvent>> buildConsumerRecordsList() {
        final List<ConsumerRecords<String, PolicyOutputEvent>> result = new ArrayList<>();

        final ConsumerRecord<String, PolicyOutputEvent> record1 = TestDataBuilder.buildConsumerRecord("1", 1l, 0, 0);
        final ConsumerRecord<String, PolicyOutputEvent> record2 = TestDataBuilder.buildConsumerRecord("2", 5l, 0, 1);
        final ConsumerRecord<String, PolicyOutputEvent> record3 = TestDataBuilder.buildConsumerRecord("3", 100l, 1, 0);
        final ConsumerRecord<String, PolicyOutputEvent> record4 = TestDataBuilder.buildConsumerRecord("2", 101l, 1, 1);

        final ConsumerRecord<String, PolicyOutputEvent> record5 = TestDataBuilder.buildConsumerRecord("2", 42l, 0, 2);
        final ConsumerRecord<String, PolicyOutputEvent> record6 = TestDataBuilder.buildConsumerRecord("2", 107l, 1, 2);
        final ConsumerRecord<String, PolicyOutputEvent> record7 = TestDataBuilder.buildConsumerRecord("2", 62l, 1, 3);
        final ConsumerRecord<String, PolicyOutputEvent> record8 = TestDataBuilder.buildConsumerRecord("2", 2l, 0, 3);
        final ConsumerRecord<String, PolicyOutputEvent> record9 = TestDataBuilder.buildConsumerRecord("2", 101l, 1, 4);

        final HashMap<TopicPartition, List<ConsumerRecord<String, PolicyOutputEvent>>> recordsMap1 = new HashMap<>(2);
        recordsMap1.put(new TopicPartition("topic", 0), Arrays.asList(record1, record2));
        recordsMap1.put(new TopicPartition("topic", 1), Arrays.asList(record3, record4));

        final HashMap<TopicPartition, List<ConsumerRecord<String, PolicyOutputEvent>>> recordsMap2 = new HashMap<>(2);
        recordsMap2.put(new TopicPartition("topic", 0), Arrays.asList(record2, record5));
        recordsMap2.put(new TopicPartition("topic", 1), Arrays.asList(record4, record6, record7));

        final HashMap<TopicPartition, List<ConsumerRecord<String, PolicyOutputEvent>>> recordsMap3 = new HashMap<>(2);
        recordsMap3.put(new TopicPartition("topic", 0), Collections.singletonList(record8));
        recordsMap3.put(new TopicPartition("topic", 1), Collections.singletonList(record9));

        final ConsumerRecords<String, PolicyOutputEvent> records1 = new ConsumerRecords<>(recordsMap1);
        final ConsumerRecords<String, PolicyOutputEvent> records2 = new ConsumerRecords<>(recordsMap2);
        final ConsumerRecords<String, PolicyOutputEvent> records3 = new ConsumerRecords<>(recordsMap3);

        result.add(records1);
        result.add(records2);
        result.add(records3);

        return result;
    }
}
