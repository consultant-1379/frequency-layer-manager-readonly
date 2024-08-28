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

package com.ericsson.oss.services.sonom.flm.settings;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAExecutionHandler;


/**
 * Unit tests for {@link PAExecutionHandler} class.
 */
public class PAExecutionHandlerTest {

    private static final String EXECUTION_ID = "FLM_1600701252-162";

    @Test
    public void whenGetPAExecutionsIsCalled_thenGetPAExecutionsMethodIsInvoked() throws SQLException {
        final PAExecutionDaoImpl paExecutionDaoMock = mock(PAExecutionDaoImpl.class);
        final PAExecutionHandler paExecutionHandler = new PAExecutionHandler(paExecutionDaoMock);
        doReturn(null).when(paExecutionDaoMock).getPAExecutions("EXECUTION_ID");
        paExecutionHandler.getPAExecutions(EXECUTION_ID);
        verify(paExecutionDaoMock, times(1)).getPAExecutions(EXECUTION_ID);
    }

}