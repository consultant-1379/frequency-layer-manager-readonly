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

package com.ericsson.oss.services.sonom.flm.database.pa.execution;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Interface defining the {@link PAExecutionDao}.
 */
public interface PAExecutionDao {
    /**
     * This method inserts the PA execution in the database.
     *
     * @param paExecution a {@link PAExecution} containing the paExecution which is to be added.
     * @return {@link String} containing paExecutionId of newly created record.
     * @throws SQLException SqlException thrown when there is issue updating database.
     */
    String insert(PAExecution paExecution) throws SQLException;

    /**
     * This method updates the PA execution in the database.
     *
     * @param paExecution a {@link PAExecution} containing the paExecution which is to be updated.
     * @return {@link Integer} containing number of successful rows updated otherwise zero.
     * @throws SQLException SqlException thrown when there is issue updating database.
     */
    int update(PAExecution paExecution) throws SQLException;

    /**
     * Get a list of {@link PAExecution} for an FLM execution based on flmExecutionid.
     *
     * @param flmExecutionId a {@link String} containing the wanted record id.
     * @return List of {@link PAExecution} for the given flmExecutionid or empty List if no PAExecutions returned from query.
     * @throws SQLException this exception will be raised if any error occurs during database retrieval.
     */
    List<PAExecution> getPAExecutions(String flmExecutionId) throws SQLException;

    /**
     * Method to retrieve all PA executions in the states requested, or an empty map if no PA executions exist.
     *
     * @param paExecutionStates a {@link String} array containing the {@link PAExecutionState}
     * @return Map of FlmExecutionId as key and {@link List} of {@link PAExecution} related to the FlmExecutions, as values
     * @throws SQLException this exception will be raised if any error occurs during database retrieval
     */
    Map<String, List<PAExecution>> getPAExecutionsInStates(PAExecutionState... paExecutionStates) throws SQLException;
}