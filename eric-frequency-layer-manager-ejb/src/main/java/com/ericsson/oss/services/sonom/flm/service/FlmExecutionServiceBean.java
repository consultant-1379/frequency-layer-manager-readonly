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

package com.ericsson.oss.services.sonom.flm.service;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Stateless;

import com.ericsson.oss.services.sonom.flm.service.api.FlmExecutionService;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.settings.FlmExecutionHandler;

/**
 * Implementation of {@link FlmExecutionService}.
 */
@Stateless(name = "flmExecutionServiceBean")
public class FlmExecutionServiceBean implements FlmExecutionService {

    private final FlmExecutionHandler flmExecutionHandler = new FlmExecutionHandler();

    @Override
    public Execution getExecution(final String id) throws SQLException {
        return flmExecutionHandler.getExecution(id);
    }

    @Override
    public List<ExecutionSummary> getExecutionSummaries() throws SQLException {
        return flmExecutionHandler.getAllExecutionSummaries();
    }
}
