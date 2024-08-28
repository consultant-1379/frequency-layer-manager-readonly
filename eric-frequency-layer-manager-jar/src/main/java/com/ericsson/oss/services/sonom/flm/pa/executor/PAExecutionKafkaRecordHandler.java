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

package com.ericsson.oss.services.sonom.flm.pa.executor;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * The PAExecutionKafkaRecordHandler persists the received PaPolicyOutputEvent message to database.
 */
public class PAExecutionKafkaRecordHandler implements KafkaRecordHandler<PaPolicyOutputEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionKafkaRecordHandler.class);
    private final PAOutputEventDao paOutputEventDao;

    public PAExecutionKafkaRecordHandler(final PAOutputEventDao paOutputEventDao) {
        this.paOutputEventDao = paOutputEventDao;
    }

    /**
     * Persists the received PaPolicyOutputEvent message to database.
     *
     * @param record
     *            A {@link PaPolicyOutputEvent} object, which should be persisted
     */
    @Override
    public void handle(final PaPolicyOutputEvent record) {
        try {
            LOGGER.info("Insert output event record for PA Execution ID {} and FLM Execution ID {}",
                    record.getPaExecutionId(), record.getFlmExecutionId());
            paOutputEventDao.insertPaPolicyOutputEvent(record);
        } catch (final SQLException e) {
            LOGGER.error("Failed to persist PA output event for a sector {}, for the PA Execution {}",
                    record.getPaExecutionId(), record.getSector().getSectorId(), e);
        }
    }
}
