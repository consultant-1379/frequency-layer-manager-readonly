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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.Collections;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;

/**
 * Unit tests for {@link FlmExecutionHandler} class.
 */
public class FlmExecutionHandlerTest {

    private static final String EXECUTION_ID = "FLM_1600701252-162";

    @Test
    public void whenGetAllExecutionIsCalled_thenGetAllMethodIsInvoked() throws SQLException {
        final ExecutionDaoImpl executionDaoMock = mock(ExecutionDaoImpl.class);
        final FlmExecutionHandler flmExecutionHandler = new FlmExecutionHandler();
        flmExecutionHandler.executionDao = executionDaoMock;

        doReturn(Collections.emptyList()).when(executionDaoMock).getAllSummaries();
        assertThat(flmExecutionHandler.getAllExecutionSummaries()).isEmpty();
        verify(executionDaoMock, times(1)).getAllSummaries();
    }

    @Test
    public void whenGetExecutionIsCalled_thenGetMethodIsInvoked() throws SQLException {
        final ExecutionDaoImpl executionDaoMock = mock(ExecutionDaoImpl.class);
        final FlmExecutionHandler flmExecutionHandler = new FlmExecutionHandler();
        flmExecutionHandler.executionDao = executionDaoMock;
        doReturn(null).when(executionDaoMock).get(EXECUTION_ID);
        assertThat(flmExecutionHandler.getExecution(EXECUTION_ID)).isNull();
        verify(executionDaoMock, times(1)).getWithoutRetry(EXECUTION_ID);
    }

}