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

package com.ericsson.oss.services.sonom.flm.messagehandler;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.ericsson.oss.services.sonom.common.kafka.consumer.CommitType;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;

/**
 * This interface was created to be able to mock the ManualCommitBatchKafkaConsumer in tests. It contains the methods of AbstractKafkaConsumer and
 * setKafkaRecordHandler method that is used to be able to handle Kafka messages in ExecutionConsumer
 *
 * @param <T>
 *            - type of record to be consumed
 */
public interface KafkaConsumerWrapper<T> {
    void consumeRecords();

    void setKafkaRecordHandler(KafkaRecordHandler<ConsumerRecords<String, T>> recordHandler) throws BadSetupException;

    void commitRecords(CommitType commitType, Map<Integer, ConsumerRecord<String, T>> partitionOffsetMapping);

    void shutdown();
}
