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

package com.ericsson.oss.services.sonom.flm.service;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Stateless;

import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAExecutionHandler;
import com.ericsson.oss.services.sonom.flm.service.api.PAExecutionService;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Implementation of {@link PAExecutionService}.
 */
@Stateless(name = "PAExecutionServiceBean")
public class PAExecutionServiceBean implements PAExecutionService {

    private final PAExecutionHandler flmPAExecutionHandler = new PAExecutionHandler();

    @Override
    public List<PAExecution> getPAExecutions(final String flmExecutionId) throws SQLException {
        return flmPAExecutionHandler.getPAExecutions(flmExecutionId);
    }

}
