/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */


package com.ericsson.oss.services.sonom.flm.service.startup.executions;

import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Class used to return new instance for {@link FlmAlgorithmExecutor}.
 */
public class FlmExecution {

    FlmAlgorithmExecutor getNewFlmAlgorithmExecutor(final Execution execution) {
        return new FlmAlgorithmExecutor(execution);
    }
}
