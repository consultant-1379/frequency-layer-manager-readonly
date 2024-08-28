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
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * This class is handling a batch of kafka messages that contains PaPolicyOutputEvents by implementing {@link KafkaRecordHandler}. It uses a
 * {@link KafkaConsumerWrapper} that is used to be able to test the system. It also uses a {@link KafkaRecordHandler} object to pass the Kafka
 * messages to the client for processing. Some Kafka messages will be dropped based on the executionNeeded and counters objects. The class also
 * calculates the commits that should be sent to Kafka.
 */
public class PAExecutionConsumer extends GenericExecutionConsumer<PaPolicyOutputEvent> {

    public PAExecutionConsumer(final int consumerId, final ConcurrentMap<String, ExecutionCounter> counters,
            final KafkaRecordHandler<PaPolicyOutputEvent> kafkaRecordHandler) throws BadSetupException {
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
    protected void handleRecord(final ConsumerRecord<String, PaPolicyOutputEvent> record) {
        final PaPolicyOutputEvent papoe = record.value();
        final Sector sector = papoe.getSector();
        LOGGER.info("Handling PAPolicyOutputEvent for PA execution {} and sector {}", papoe.getPaExecutionId(), sector.getSectorId());

        if (!isValidEvent(record)) {
            return;
        }

        final ExecutionCounter counter = getCounters().get(papoe.getPaExecutionId());
        if (counter == null) {
            LOGGER.warn("Ignoring PAPolicyOutputEvent for PA execution {} and sector {} partition {} offset {} in " +
                    "Kafka because no counter exists in consumer {}",
                    papoe.getPaExecutionId(), sector.getSectorId(), record.partition(), record.offset(), getConsumerId());
            return;
        }
        if (counter.getCount() == 0) {
            LOGGER.warn("Ignoring PAPolicyOutputEvent because count == 0, for PA execution {} and sector {} partition {} offset {} in consumer {}",
                    papoe.getPaExecutionId(), sector.getSectorId(), record.partition(), record.offset(), getConsumerId());
            return;
        }
        LOGGER.info("Handling PAPolicyOutputEvent for PA execution {} and sector {} partition {} offset {} in consumer {}",
                papoe.getPaExecutionId(), sector.getSectorId(), record.partition(), record.offset(), getConsumerId());
        getKafkaRecordHandler().handle(papoe);
        LOGGER.info("Counting PAPolicyOutputEvent for PA execution {} and sector {} partition {} offset {} in consumer {}",
                papoe.getPaExecutionId(), sector.getSectorId(), record.partition(), record.offset(), getConsumerId());
        counter.countDown(sector.getSectorIdAsLong());
    }

    boolean isValidEvent(final ConsumerRecord<String, PaPolicyOutputEvent> record) {
        final PaPolicyOutputEvent papoe = record.value();

        if (papoe.getPaExecutionId() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Ignoring PAPolicyOutputEvent for PA execution {} and sector {} partition {} offset {} in " +
                        "Kafka because execution id is null in consumer {} message {}",
                        papoe.getPaExecutionId(), papoe.getSector().getSectorId(), record.partition(), record.offset(), getConsumerId(),
                        record.toString());
            }
            return false;
        }

        if (papoe.getSector().getSectorId() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Ignoring PAPolicyOutputEvent for PA execution {} and sector {} partition {} offset {} in " +
                        "Kafka because sector id is null in consumer {} message {}",
                        papoe.getPaExecutionId(), papoe.getSector().getSectorId(), record.partition(), record.offset(), getConsumerId(),
                        record.toString());
            }
            return false;
        }
        return true;
    }

}