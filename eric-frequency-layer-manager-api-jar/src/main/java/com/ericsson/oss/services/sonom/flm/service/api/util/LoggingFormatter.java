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

package com.ericsson.oss.services.sonom.flm.service.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions for logging according to schema.
 */
public class LoggingFormatter {
    public static final String EXECUTION_ID_MSG = "Execution_ID: %s, %s";
    public static final String EXECUTION_ID_SECTOR_ID_MSG = "Execution_ID: %s, Sector_ID: %d, %s";
    public static final String EXECUTION_ID_SECTOR_ID_EXCLUSION_MSG = "Execution_ID: {}, Sector_ID: {}, Exclusion_Reason: {}";
    public static final String EXECUTION_ID_SECTOR_ID_CELL_ID_EXCLUSION_MSG = "Execution_ID: {}, Oss_ID: {}, Sector_ID: {}, " +
     "Cell_ID: {}, Exclusion_Reason: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFormatter.class);

    private LoggingFormatter() {
        // Intentionally private.
    }

    /**
     * Ensures the filtering of Sectors adheres to logging schema.
     *
     * @param executionId
     *            The id of the current execution.
     * @param sectorId
     *            The id of the sector.
     * @param errorMessage
     *            The reason for the topology object being filtered out.
     */
    public static void logFilteredSector(final String executionId, final String sectorId, final String errorMessage) {
        LOGGER.info(EXECUTION_ID_SECTOR_ID_EXCLUSION_MSG, executionId, sectorId, errorMessage);
    }

    /**
     * Ensures the filtering of Cells adheres to logging schema.
     *
     * @param executionId
     *            The id of the current execution.
     * @param ossId
     *            The oss id of associated with the object being filtered out.
     * @param sectorId
     *            The sectorId the cell belongs to.
     * @param cellId
     *            The id of the topology object being filtered out (FDN).
     * @param errorMessage
     *            The reason for the topology object being filtered out.
     */
    public static void logFilteredCell(final String executionId, final String ossId, final String sectorId, final String cellId,
            final String errorMessage) {
        LOGGER.info(EXECUTION_ID_SECTOR_ID_CELL_ID_EXCLUSION_MSG, executionId, ossId, sectorId, cellId, errorMessage);
    }

    /**
     * Ensures the logging adheres to logging schema.
     *
     * @param executionId
     *            The id of the current execution.
     * @param sectorId
     *            The id of the sector.
     * @param message
     *            The message to be formatted.
     * @return
     *            The message to be logged.
     */
    public static String formatMessage(final String executionId, final Long sectorId, final String message) {
        return String.format(EXECUTION_ID_SECTOR_ID_MSG, executionId, sectorId, message);
    }

    /**
     * Ensures the logging adheres to logging schema.
     *
     * @param executionId
     *            The id of the current execution.
     * @param message
     *            The message to be formatted.
     * @return
     *            The message to be logged.
     */
    public static String formatMessage(final String executionId, final String message) {
        return String.format(EXECUTION_ID_MSG, executionId, message);
    }

}
