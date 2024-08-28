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

package com.ericsson.oss.services.sonom.flm.database.execution;

import java.sql.SQLException;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;

/**
 * Interface defining the <code>ExecutionDao</code>.
 */
public interface ExecutionDao {

    /**
     * This method inserts the FLM execution in the database.
     *
     * @param execution {@link Execution} the execution which is to be added.
     * @return String execution id of newly created record.
     * @throws SQLException SqlException thrown when there is issue updating database.
     */
    String insert(Execution execution) throws SQLException;

    /**
     * This method updates the FLM execution in the database.
     *
     * @param execution {@link Execution} the execution which is to be updated.
     * @return int number of successful rows updated otherwise zero.
     * @throws SQLException SqlException thrown when there is issue updating database.
     */
    int update(Execution execution) throws SQLException;

    /**
     * Get a single execution for the FLM algorithm based on id.
     *
     * @param id {@link String} the wanted record id.
     * @return {@link Execution} containing the execution or {@link SQLException} if the execution does not exist
     * @throws SQLException this exception will be raised if any error occurs during database retrieval
     */
    Execution get(String id) throws SQLException;

    /**
     * Get a single execution for the FLM algorithm based on id.
     *
     * @param id {@link String} the wanted record id.
     * @return {@link Execution} containing the execution or null if the execution does not exist.
     * @throws SQLException this exception will be raised if any error occurs during database retrieval.
     */
    Execution getWithoutRetry(String id) throws SQLException;

    /**
     * Get all execution summaries for the FLM algorithm.
     *
     * @return {@link List} of {@link ExecutionSummary} containing the records or an empty list if no executions exist
     * @throws SQLException this exception will be raised if any error occurs during database retrieval
     */
    List<ExecutionSummary> getAllSummaries() throws SQLException;

    /**
     * Method to retrieve all executions in the states requested or an empty list if no executions exist.
     *
     * @param executionStates The {@link ExecutionState}'s to return
     * @return {@link List} of {@link Execution}'s
     * @throws SQLException this exception will be raised if any error occurs during database retrieval
     */
    List<Execution> getExecutionsInStates(ExecutionState... executionStates) throws SQLException;
}
