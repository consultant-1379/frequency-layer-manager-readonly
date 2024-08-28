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

import java.util.concurrent.ConcurrentMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class is handling a batch of kafka messages that contains FLM PolicyOutputEvents by extending {@link GenericExecutionConsumer}. It uses a
 * {@link KafkaConsumerWrapper} that is used to be able to test the system. It also uses a {@link KafkaRecordHandler} object to pass the Kafka
 * messages to the client for processing. Some Kafka messages will be dropped based on the executionNeeded and counters objects. The class also
 * calculates the commits that should be sent to Kafka.
 */

public class PoeExecutionConsumer extends GenericExecutionConsumer<PolicyOutputEvent> {

    public PoeExecutionConsumer(final int consumerId, final ConcurrentMap<String, ExecutionCounter> counters,
            final KafkaRecordHandler<PolicyOutputEvent> kafkaRecordHandler) throws BadSetupException {
        super(consumerId, counters, kafkaRecordHandler);
    }

    /**
     * It handles one Kafka message. It either drops or handles the message by calling kafkaRecordHandler provider by client. The message belongs to
     * an execution. The decision if the message should be dropped depends on if the execution is needed or not and whether counter can be found for
     * the Execution or not.
     *
     * @param record
     *            the Kafka message
     */
    @Override
    protected void handleRecord(final ConsumerRecord<String, PolicyOutputEvent> record) {
        final PolicyOutputEvent poe = record.value();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(), "Handling PolicyOutputEvent"));
        }

        if (!isValidEvent(record)) {
            return;
        }

        final ExecutionCounter counter = getCounters().get(poe.getExecutionId());

        if (counter == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                        String.format("Ignoring PolicyOutputEvent partition %d offset %d in Kafka because no counter exists in consumer %d",
                                record.partition(), record.offset(), getConsumerId())));
            }
            return;
        }

        if (counter.getCount() == 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                        String.format("Ignoring PolicyOutputEvent because count == 0, partition %d offset %d in consumer %d",
                                record.partition(), record.offset(), getConsumerId())));
            }
            return;
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                    String.format("Handling PolicyOutputEvent partition %d offset %d in consumer %d",
                            record.partition(), record.offset(), getConsumerId())));
        }

        getKafkaRecordHandler().handle(poe);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                    String.format("Counting PolicyOutputEvent partition %d offset %d in consumer %d",
                            record.partition(), record.offset(), getConsumerId())));
        }
        counter.countDown(record.value().getSectorId());
    }

    boolean isValidEvent(final ConsumerRecord<String, PolicyOutputEvent> record) {
        final PolicyOutputEvent poe = record.value();

        if (poe.getExecutionId() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                        String.format(
                                "Ignoring PolicyOutputEvent partition %d offset %d in Kafka because execution id is null in consumer %d message %s",
                                record.partition(), record.offset(), getConsumerId(), record.toString())));
            }
            return false;
        }

        if (poe.getSectorId() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                        String.format(
                                "Ignoring PolicyOutputEvent partition %d offset %d in Kafka because sector id is null in consumer %d message %s",
                                record.partition(), record.offset(), getConsumerId(), record.toString())));
            }
            return false;
        }
        return true;
    }

}