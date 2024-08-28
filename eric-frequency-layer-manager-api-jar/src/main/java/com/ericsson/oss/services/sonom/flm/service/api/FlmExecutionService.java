/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.sonom.flm.service.api;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Remote;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;

/**
 * Interface defining the methods which we can get the {@link Execution} object(s) from the database.
 */
@Remote
public interface FlmExecutionService {

    /**
     * Get all execution summaries.
     *
     * @return {@link List} of {@link ExecutionSummary} containing all of the executions.
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    List<ExecutionSummary> getExecutionSummaries() throws SQLException;

    /**
     * Get execution by id.
     *
     * @param executionId
     *      the id of the {@link Execution}
     * @return an {@link Execution} containing an execution with the specified id
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    Execution getExecution(String executionId) throws SQLException;
}
