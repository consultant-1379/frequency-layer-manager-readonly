/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.optimization;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * The ExecutionKafkaRecordHandler persists the received PolicyOutputEvent message to database.
 */
public class ExecutionKafkaRecordHandler implements KafkaRecordHandler<PolicyOutputEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionKafkaRecordHandler.class);
    private final OptimizationsDao optimizationsDao;

    public ExecutionKafkaRecordHandler(final OptimizationsDao optimizationsDao) {
        this.optimizationsDao = optimizationsDao;
    }

    /**
     *   Persists the received PolicyOutputEvent message to database.
     *
     *   @param record A {@link PolicyOutputEvent} object, which should be persisted
     */
    @Override
    public void handle(final PolicyOutputEvent record) {
        try {
              if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(record.getExecutionId(), record.getSectorId(), "Insert optimization record"));
              }
              optimizationsDao.insertOptimization(record);
        } catch (SQLException e) {
              if (LOGGER.isErrorEnabled()) {
                LOGGER.error(LoggingFormatter.formatMessage(record.getExecutionId(), record.getSectorId(), "Optimization Database error"), e);
              }
        }
    }

}