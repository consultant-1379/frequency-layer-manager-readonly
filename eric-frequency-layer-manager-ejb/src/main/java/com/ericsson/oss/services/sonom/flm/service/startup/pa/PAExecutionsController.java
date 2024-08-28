/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.startup.pa;

import javax.ejb.Local;

/**
 * Implementations of this interface will determine the state of PA executions on start up and schedule again if needed.
 */
@Local
public interface PAExecutionsController {

    /**
     * Schedule PA Execution on startup that have not run.
     */
    void schedulePAExecutions();
}
