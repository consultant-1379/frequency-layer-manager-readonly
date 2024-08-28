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

package com.ericsson.oss.services.sonom.flm.settings;

import java.sql.SQLException;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;

/**
 * Class that is used to get FLM executions settings.
 */
public class FlmExecutionHandler {

    ExecutionDao executionDao = new ExecutionDaoImpl(1, 10);

    /**
     * Get all execution summaries.
     *
     * @return {@link List} of {@link ExecutionSummary} containing all of the executions.
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    public List<ExecutionSummary> getAllExecutionSummaries() throws SQLException {
        return executionDao.getAllSummaries();
    }

    /**
     * Get execution by id.
     *
     * @param executionId
     *      the id of the {@link Execution}
     * @return an {@link Execution} containing an execution with the specified id
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    public Execution getExecution(final String executionId) throws SQLException {
        return executionDao.getWithoutRetry(executionId);
    }
}
