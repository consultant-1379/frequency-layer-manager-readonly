/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Interface defining the methods which we can get the {@link PAExecution} object(s) from the database.
 */
@Remote
public interface PAExecutionService {
    /**
     * Get PA execution by flm execution id.
     *
     * @param flmExecutionId
     *      the id of the {@link Execution}
     * @return {@link List} of {@link PAExecution} containing all of the PA executions.
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    List<PAExecution> getPAExecutions(String flmExecutionId) throws SQLException;

}
